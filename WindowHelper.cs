using Guna.UI2.WinForms;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Runtime.InteropServices;
using System.Windows.Forms;

namespace Nelian
{
    public static class WindowHelper
    {
        [DllImport("Gdi32.dll", EntryPoint = "CreateRoundRectRgn")]
        private static extern IntPtr CreateRoundRectRgn(
            int nLeftRect,
            int nTopRect,
            int nRightRect,
            int nBottomRect,
            int nWidthEllipse,
            int nHeightEllipse);

        public static void FixUI(Control parent)
        {
            Apply(parent);

            foreach (Control c in parent.Controls)
                FixUI(c);
        }

        private static void Apply(Control c)
        {
            if (c is Label lbl)
            {
                lbl.BackColor = Color.Transparent;
                return;
            }

            if (c is Guna2Button gbtn)
            {
                gbtn.FillColor = Color.FromArgb(40, 40, 40);
                gbtn.ForeColor = Color.White;
                gbtn.BackColor = Color.Transparent;
                gbtn.HoverState.FillColor = Color.FromArgb(60, 60, 60);
                gbtn.HoverState.ForeColor = Color.White;
                gbtn.PressedColor = Color.FromArgb(25, 25, 25);
                gbtn.DisabledState.ForeColor = Color.White;
                gbtn.DisabledState.FillColor = Color.FromArgb(40, 40, 40);
                return;
            }

            if (c is Guna2Panel panel)
            {
                panel.FillColor = Color.FromArgb(30, 30, 30);
                return;
            }

            if (c is Guna2ComboBox combo)
            {
                combo.FillColor = Color.FromArgb(40, 40, 40);
                combo.ForeColor = Color.White;
                combo.BorderColor = Color.FromArgb(60, 60, 60);
                return;
            }

            if (c is Guna2TextBox textBox)
            {
                textBox.FillColor = Color.FromArgb(40, 40, 40);
                textBox.ForeColor = Color.White;
                textBox.BorderColor = Color.FromArgb(60, 60, 60);
                return;
            }
        }

        public static void ApplyRoundedCorners(Form form, int radius)
        {
            form.FormBorderStyle = FormBorderStyle.None;
            ApplyRegion(form, radius);
        }

        public static void EnableResizeFix(Form form, int radius)
        {
            form.Resize += (s, e) => ApplyRegion(form, radius);
        }

        private static void ApplyRegion(Form form, int radius)
        {
            if (form.Width <= 0 || form.Height <= 0) return;

            form.Region = Region.FromHrgn(CreateRoundRectRgn(
                0, 0,
                form.Width,
                form.Height,
                radius,
                radius));
        }

        [DllImport("user32.dll")]
        private static extern bool ReleaseCapture();

        [DllImport("user32.dll")]
        private static extern int SendMessage(IntPtr hWnd, int Msg, int wParam, int lParam);

        private const int WM_NCLBUTTONDOWN = 0xA1;
        private const int HTCAPTION = 0x2;

        public static void EnableDrag(Form form, Control dragArea)
        {
            dragArea.MouseDown += (s, e) =>
            {
                if (e.Button == MouseButtons.Left)
                {
                    ReleaseCapture();
                    SendMessage(form.Handle, WM_NCLBUTTONDOWN, HTCAPTION, 0);
                }
            };
        }

        [DllImport("dwmapi.dll")]
        private static extern int DwmSetWindowAttribute(IntPtr hwnd, int attr, ref int attrValue, int attrSize);

        [DllImport("dwmapi.dll")]
        private static extern int DwmExtendFrameIntoClientArea(IntPtr hwnd, ref MARGINS margins);

        public static void EnableShadow(Form form)
        {
            form.Load += (s, e) =>
            {
                try
                {
                    int attr = 2;
                    DwmSetWindowAttribute(form.Handle, 2, ref attr, 4);

                    MARGINS margins = new MARGINS
                    {
                        cxLeftWidth = 1,
                        cxRightWidth = 1,
                        cyTopHeight = 1,
                        cyBottomHeight = 1
                    };

                    DwmExtendFrameIntoClientArea(form.Handle, ref margins);
                }
                catch { }
            };
        }

        [StructLayout(LayoutKind.Sequential)]
        private struct MARGINS
        {
            public int cxLeftWidth;
            public int cxRightWidth;
            public int cyTopHeight;
            public int cyBottomHeight;
        }

        public static void ClearAnimator(Form form)
        {
            if (animators.TryGetValue(form, out var animator))
            {
                animators.Remove(form);
            }
        }

        private class Animator
        {
            private void Finish()
            {
                form.Size = target;
                ApplyRegion(form, radius);

                running = false;
                timer.Stop();
                timer.Dispose();
                WindowHelper.ClearAnimator(form);
            }

            private readonly System.Windows.Forms.Timer timer;
            private readonly Form form;
            private readonly int radius;
            private readonly float speed;

            private Size target;
            private bool running;

            public Animator(Form form, int radius, float speed)
            {
                this.form = form;
                this.radius = radius;
                this.speed = speed;

                timer = new System.Windows.Forms.Timer();
                timer.Interval = 15;
                timer.Tick += Tick;
            }

            public void Start(Size target)
            {
                this.target = target;
                running = true;

                if (!timer.Enabled)
                    timer.Start();
            }

            private void Tick(object sender, EventArgs e)
            {
                if (!running)
                {
                    timer.Stop();
                    timer.Dispose();
                    return;
                }

                int oldW = form.Width;
                int oldH = form.Height;

                int newW = Lerp(oldW, target.Width, speed);
                int newH = Lerp(oldH, target.Height, speed);

                int dx = (newW - oldW) / 2;
                int dy = (newH - oldH) / 2;

                form.Location = new Point(
                    form.Location.X - dx,
                    form.Location.Y - dy
                );

                form.Size = new Size(newW, newH);
                

                bool done =
                    Math.Abs(newW - target.Width) < 2 &&
                    Math.Abs(newH - target.Height) < 2;

                if (done)
                {
                    form.Size = target;
                    ApplyRegion(form, radius);
                    running = false;
                    timer.Stop();
                    timer.Dispose();
                    WindowHelper.ClearAnimator(form);
                }
            }

            private int Lerp(int start, int end, float t)
            {
                return (int)(start + (end - start) * t);
            }
        }

        private static readonly Dictionary<Form, Animator> animators = new();

        public static void AnimateResize(Form form, Size size, int radius = 18, float speed = 0.2f)
        {
            if (form == null) return;

            if (!animators.TryGetValue(form, out var animator))
            {
                animator = new Animator(form, radius, speed);
                animators[form] = animator;
            }

            animator.Start(size);
        }

        [DllImport("user32.dll")]
        private static extern IntPtr SendMessage(IntPtr hWnd, int msg, IntPtr wParam, IntPtr lParam);

        private const int WM_SETREDRAW = 0x000B;

        public static void SuspendControl(Control control)
        {
            if (control == null || !control.IsHandleCreated)
                return;

            SendMessage(control.Handle, WM_SETREDRAW, IntPtr.Zero, IntPtr.Zero);
        }

        public static void ResumeControl(Control control)
        {
            if (control == null || !control.IsHandleCreated)
                return;

            SendMessage(control.Handle, WM_SETREDRAW, new IntPtr(1), IntPtr.Zero);
            control.Invalidate();
            control.Update();
        }

        public static void ApplyModernWindow(Form form, Control dragArea, int radius = 18)
        {
            form.FormBorderStyle = FormBorderStyle.None;

            ApplyRoundedCorners(form, radius);
            EnableResizeFix(form, radius);
            EnableDrag(form, dragArea);
            EnableShadow(form);
        }
    }
}
