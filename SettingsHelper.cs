using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;

namespace Nelian
{
    public enum LaunchBehavior
    {
        KeepOpen,
        Minimize,
        Close
    }

    public static class SettingsHelper
    {
        public static string ConfigPath => Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
            ".minecraft",
            "NelianLauncherProperties.txt"
        );

        private static readonly object _fileLock = new object();

        private static Dictionary<string, string> LoadRaw()
        {
            var dict = new Dictionary<string, string>(StringComparer.OrdinalIgnoreCase);
            try
            {
                if (!File.Exists(ConfigPath))
                    return dict;

                foreach (var line in File.ReadAllLines(ConfigPath))
                {
                    if (string.IsNullOrWhiteSpace(line)) continue;
                    int idx = line.IndexOf('=');
                    if (idx <= 0) continue;

                    string key = line.Substring(0, idx).Trim();
                    string value = line.Substring(idx + 1).Trim();

                    dict[key] = value;
                }
            }
            catch
            {
                return new Dictionary<string, string>(StringComparer.OrdinalIgnoreCase);
            }
            return dict;
        }

        private static bool SaveRaw(Dictionary<string, string> values)
        {
            lock (_fileLock)
            {
                try
                {
                    string directory = Path.GetDirectoryName(ConfigPath);
                    if (!string.IsNullOrEmpty(directory))
                        Directory.CreateDirectory(directory);

                    var lines = values.Select(kv => $"{kv.Key}={kv.Value}");
                    File.WriteAllLines(ConfigPath, lines);
                    return true;
                }
                catch
                {
                    return false;
                }
            }
        }

        private static string Get(string key, string fallback)
        {
            var dict = LoadRaw();
            return dict.TryGetValue(key, out var value) && !string.IsNullOrWhiteSpace(value)
                ? value
                : fallback;
        }

        private static bool GetBool(string key, bool fallback)
        {
            var dict = LoadRaw();
            if (dict.TryGetValue(key, out var value))
                return value.Trim().Equals("true", StringComparison.OrdinalIgnoreCase);
            return fallback;
        }

        public static bool IsVanillaModeEnabled() => GetBool("VanillaMode", false);
        public static bool IsLiveAnimationsEnabled() => GetBool("LiveAnimations", false);
        public static bool IsDiscordRpcEnabled() => GetBool("DiscordRPC", true);
        public static bool IsFullscreenEnabled() => GetBool("Fullscreen", false);
        public static string GetServerIp() => Get("ServerIP", "");
        public static string GetTheme() => Get("Theme", "Newgen");
        public static string GetSelectedVersion() => Get("SelectedVersion", "1.8.8");

        public static int GetMemory()
        {
            var raw = Get("Memory", "4096");
            return int.TryParse(raw, out int mem) ? mem : 4096;
        }

        public static LaunchBehavior GetLaunchBehavior()
        {
            string value = Get("LaunchBehavior", "Minimize");
            if (Enum.TryParse<LaunchBehavior>(value, true, out var result))
                return result;
            return LaunchBehavior.Minimize;
        }
//write default
        public class SettingsSnapshot
        {
            public int Memory { get; set; }
            public bool VanillaMode { get; set; }
            public string SelectedVersion { get; set; } = "1.8.8";
            public bool LiveAnimations { get; set; }
            public string Theme { get; set; } = "Newgen";
            public bool DiscordRpc { get; set; }
            public bool Fullscreen { get; set; }
            public string ServerIp { get; set; } = "";
            public LaunchBehavior LaunchBehavior { get; set; } = LaunchBehavior.Minimize;
        }

        public static bool Save(SettingsSnapshot s)
        {
            var dict = LoadRaw();

            dict["Memory"] = s.Memory.ToString();
            dict["VanillaMode"] = s.VanillaMode ? "true" : "false";
            dict["SelectedVersion"] = s.SelectedVersion ?? "1.8.8";
            dict["LiveAnimations"] = s.LiveAnimations ? "true" : "false";
            dict["Theme"] = s.Theme ?? "Newgen";
            dict["DiscordRPC"] = s.DiscordRpc ? "true" : "false";
            dict["Fullscreen"] = s.Fullscreen ? "true" : "false";
            dict["ServerIP"] = s.ServerIp ?? "";
            dict["LaunchBehavior"] = s.LaunchBehavior.ToString();

            bool ok = SaveRaw(dict);
            if (ok)
                SettingsChanged?.Invoke(null, EventArgs.Empty);
            return ok;
        }

        public static event EventHandler SettingsChanged;
    }
}
