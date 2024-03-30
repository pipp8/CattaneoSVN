namespace CalcClient
{
    partial class Form1
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.txtOperando1 = new System.Windows.Forms.TextBox();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.txtOperando2 = new System.Windows.Forms.TextBox();
            this.label3 = new System.Windows.Forms.Label();
            this.txtRisultato = new System.Windows.Forms.TextBox();
            this.btRun = new System.Windows.Forms.Button();
            this.btMultipleCalls = new System.Windows.Forms.Button();
            this.cmbOpType = new System.Windows.Forms.ComboBox();
            this.label4 = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // txtOperando1
            // 
            this.txtOperando1.Location = new System.Drawing.Point(12, 25);
            this.txtOperando1.Name = "txtOperando1";
            this.txtOperando1.Size = new System.Drawing.Size(100, 20);
            this.txtOperando1.TabIndex = 0;
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(13, 10);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(61, 13);
            this.label1.TabIndex = 1;
            this.label1.Text = "operando 1";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(12, 70);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(61, 13);
            this.label2.TabIndex = 2;
            this.label2.Text = "operando 2";
            // 
            // txtOperando2
            // 
            this.txtOperando2.Location = new System.Drawing.Point(12, 85);
            this.txtOperando2.Name = "txtOperando2";
            this.txtOperando2.Size = new System.Drawing.Size(100, 20);
            this.txtOperando2.TabIndex = 3;
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(12, 130);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(43, 13);
            this.label3.TabIndex = 4;
            this.label3.Text = "risultato";
            // 
            // txtRisultato
            // 
            this.txtRisultato.Location = new System.Drawing.Point(12, 145);
            this.txtRisultato.Name = "txtRisultato";
            this.txtRisultato.Size = new System.Drawing.Size(100, 20);
            this.txtRisultato.TabIndex = 5;
            // 
            // btRun
            // 
            this.btRun.Location = new System.Drawing.Point(146, 85);
            this.btRun.Name = "btRun";
            this.btRun.Size = new System.Drawing.Size(118, 23);
            this.btRun.TabIndex = 6;
            this.btRun.Text = "Run";
            this.btRun.UseVisualStyleBackColor = true;
            this.btRun.Click += new System.EventHandler(this.btRun_Click);
            // 
            // btMultipleCalls
            // 
            this.btMultipleCalls.Location = new System.Drawing.Point(146, 143);
            this.btMultipleCalls.Name = "btMultipleCalls";
            this.btMultipleCalls.Size = new System.Drawing.Size(118, 23);
            this.btMultipleCalls.TabIndex = 12;
            this.btMultipleCalls.Text = "Multiple Calls";
            this.btMultipleCalls.UseVisualStyleBackColor = true;
            this.btMultipleCalls.Click += new System.EventHandler(this.btMultipleCalls_Click);
            // 
            // cmbOpType
            // 
            this.cmbOpType.FormattingEnabled = true;
            this.cmbOpType.Items.AddRange(new object[] {
            "Addizione",
            "Sottrazione",
            "Moltiplicazione",
            "Divisione",
            "GetString(int)",
            "GetObject(obj)"});
            this.cmbOpType.Location = new System.Drawing.Point(143, 24);
            this.cmbOpType.Name = "cmbOpType";
            this.cmbOpType.Size = new System.Drawing.Size(121, 21);
            this.cmbOpType.TabIndex = 13;
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(143, 9);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(59, 13);
            this.label4.TabIndex = 14;
            this.label4.Text = "operazione";
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(276, 181);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.cmbOpType);
            this.Controls.Add(this.btMultipleCalls);
            this.Controls.Add(this.btRun);
            this.Controls.Add(this.txtRisultato);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.txtOperando2);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.txtOperando1);
            this.Name = "Form1";
            this.Text = "Calc Service Client";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.TextBox txtOperando1;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.TextBox txtOperando2;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.TextBox txtRisultato;
        private System.Windows.Forms.Button btRun;
        private System.Windows.Forms.Button btMultipleCalls;
        private System.Windows.Forms.ComboBox cmbOpType;
        private System.Windows.Forms.Label label4;
    }
}

