using Shared;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.IO.Compression;
using System.Linq;
using System.Net.Http;
using System.Text.Json;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace Updater
{
    public partial class Form1 : Form
    {
        private const string MANIFEST_URL = "https://raw.githubusercontent.com/FixzyXqw/Nelian/refs/heads/main/manifest.json";
        private const string UPDATE_LINK = "https://github.com/FixzyXqw/Nelian/releases/download/uppdatee/Nelian.zip";
        private static readonly string INSTALL_DIR = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86),
            "Nelian"
        );
        private const string RUNTIME_FILE = "Nelian.runtime";

        public Form1()
        {
            InitializeComponent();
        }

        private async void Form1_Load(object sender, EventArgs e)
        {
            await PerformUpdate();
        }


        private void Open()
        {
            string exePath = Path.Combine(
    Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86),
    "Nelian",
    "Nelian.exe"
);

            if (File.Exists(exePath))
            {
                Process.Start(new ProcessStartInfo
                {
                    FileName = exePath,
                    UseShellExecute = true,
                    Verb = "runas"
                });
            }
            else
            {
                MessageBox.Show("Nelian.exe bulunamadı.");
            }
        }
        private async Task PerformUpdate()
        {
            try
            {
                label2.Text = "Checking for Updates";
                progressBar1.Value = 10;

                bool hasUpdate = await CheckManifestDifference();

                if (!hasUpdate)
                {
                    label2.Text = "Application is Up to Date";
                    Open();
                    label2.Text = "Launching..";
                    progressBar1.Value = 100;
                    await Task.Delay(1500);
                    Application.Exit();
                    return;
                }

                label2.Text = "Installing Updates...";
                progressBar1.Value = 20;

                string zipPath = Path.GetTempFileName() + ".zip";

                using HttpClient client = new HttpClient();
                client.Timeout = TimeSpan.FromMinutes(5);
                var response = await client.GetAsync(UPDATE_LINK);
                response.EnsureSuccessStatusCode();

                byte[] zipData = await response.Content.ReadAsByteArrayAsync();
                await File.WriteAllBytesAsync(zipPath, zipData);

                progressBar1.Value = 40;

                label2.Text = "Updating...";

                string runtimeBackup = null;
                string runtimePath = Path.Combine(INSTALL_DIR, RUNTIME_FILE);
                if (File.Exists(runtimePath))
                {
                    runtimeBackup = Path.GetTempFileName();
                    File.Copy(runtimePath, runtimeBackup, true);
                }

                progressBar1.Value = 50;

                DeleteAllFilesExceptRuntimeAndDotNet();

                progressBar1.Value = 60;

                ExtractZipExcludingDotNet(zipPath, INSTALL_DIR);

                progressBar1.Value = 80;

                if (runtimeBackup != null && File.Exists(runtimeBackup))
                {
                    File.Copy(runtimeBackup, runtimePath, true);
                    File.Delete(runtimeBackup);
                }

                using HttpClient client2 = new HttpClient();
                string newManifest = await client2.GetStringAsync(MANIFEST_URL);
                string manifestPath = Path.Combine(INSTALL_DIR, "manifest.json");
                await File.WriteAllTextAsync(manifestPath, newManifest);

                progressBar1.Value = 90;

                if (File.Exists(zipPath))
                    File.Delete(zipPath);

                progressBar1.Value = 100;
                label2.Text = "You're up to date!";
                Open();
                label2.Text = "Launching..";
                await Task.Delay(20000);
                Application.Exit();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Something went Wrong: {ex.Message}", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                label2.Text = "Update Failed!";
                await Task.Delay(3000);
                Application.Exit();
            }
        }

        private async Task<bool> CheckManifestDifference()
        {
            try
            {
                using HttpClient client = new HttpClient();
                client.Timeout = TimeSpan.FromSeconds(30);
                string remoteManifestJson = await client.GetStringAsync(MANIFEST_URL);
                var remoteManifest = JsonSerializer.Deserialize<Manifest>(remoteManifestJson);

                string localManifestPath = Path.Combine(INSTALL_DIR, "manifest.json");
                if (!File.Exists(localManifestPath))
                {
                    return true;
                }

                string localManifestJson = File.ReadAllText(localManifestPath);
                var localManifest = JsonSerializer.Deserialize<Manifest>(localManifestJson);

                if (remoteManifest.version != localManifest.version)
                {
                    return true;
                }

                var remoteFilesDict = new Dictionary<string, string>();
                foreach (var file in remoteManifest.files)
                {
                    if (RuntimeFilter.IsDotNetRuntimeFile(file.path))
                        continue;
                    remoteFilesDict[file.path] = file.sha1;
                }

                var localFilesDict = new Dictionary<string, string>();
                foreach (var file in localManifest.files)
                {
                    if (RuntimeFilter.IsDotNetRuntimeFile(file.path))
                        continue;
                    localFilesDict[file.path] = file.sha1;
                }

                if (remoteFilesDict.Count != localFilesDict.Count)
                {
                    return true;
                }

                foreach (var kvp in remoteFilesDict)
                {
                    if (!localFilesDict.ContainsKey(kvp.Key) || localFilesDict[kvp.Key] != kvp.Value)
                    {
                        return true;
                    }
                }

                return false;
            }
            catch
            {
                return true;
            }
        }

        private void DeleteAllFilesExceptRuntimeAndDotNet()
        {
            if (!Directory.Exists(INSTALL_DIR))
                return;

            foreach (string file in Directory.GetFiles(INSTALL_DIR))
            {
                string fileName = Path.GetFileName(file);

                if (fileName == RUNTIME_FILE)
                    continue;

                if (fileName == "manifest.json")
                    continue;

                if (RuntimeFilter.IsDotNetRuntimeFile(fileName))
                    continue;

                try { File.Delete(file); } catch { }
            }

            foreach (string dir in Directory.GetDirectories(INSTALL_DIR))
            {
                string dirName = Path.GetFileName(dir);

                bool isLanguageFolder = dirName.Length == 2 ||
                                       dirName == "zh-Hans" ||
                                       dirName == "zh-Hant" ||
                                       dirName == "pt-BR";

                if (isLanguageFolder)
                {
                    CleanLanguageFolder(dir);
                }
                else
                {
                    try { Directory.Delete(dir, true); } catch { }
                }
            }
        }

        private void CleanLanguageFolder(string folderPath)
        {
            if (!Directory.Exists(folderPath))
                return;

            foreach (string file in Directory.GetFiles(folderPath))
            {
                try { File.Delete(file); } catch { }
            }

            try { Directory.Delete(folderPath); } catch { }
        }

        private void ExtractZipExcludingDotNet(string zipPath, string extractPath)
        {
            if (!Directory.Exists(extractPath))
                Directory.CreateDirectory(extractPath);

            using ZipArchive archive = ZipFile.OpenRead(zipPath);
            foreach (ZipArchiveEntry entry in archive.Entries)
            {
                if (string.IsNullOrEmpty(entry.Name))
                {
                    string dirPath = Path.Combine(extractPath, entry.FullName);
                    if (!Directory.Exists(dirPath))
                        Directory.CreateDirectory(dirPath);
                    continue;
                }

               

                if (entry.Name == RUNTIME_FILE)
                    continue;

                string fullPath = Path.Combine(extractPath, entry.FullName);
                string dirName = Path.GetDirectoryName(fullPath);

                if (!string.IsNullOrEmpty(dirName) && !Directory.Exists(dirName))
                    Directory.CreateDirectory(dirName);

                entry.ExtractToFile(fullPath, true);
            }
        }

        private class Manifest
        {
            public string version { get; set; }
            public List<FileEntry> files { get; set; }
        }

        private class FileEntry
        {
            public string path { get; set; }
            public string sha1 { get; set; }
        }
    }
}
