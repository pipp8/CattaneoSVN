namespace TestAIRClient
{
    partial class Form1
    {
        /// <summary>
        /// Variabile di progettazione necessaria.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Liberare le risorse in uso.
        /// </summary>
        /// <param name="disposing">ha valore true se le risorse gestite devono essere eliminate, false in caso contrario.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Codice generato da Progettazione Windows Form

        /// <summary>
        /// Metodo necessario per il supporto della finestra di progettazione. Non modificare
        /// il contenuto del metodo con l'editor di codice.
        /// </summary>
        private void InitializeComponent()
        {
            this.btAggiungiPaziente = new System.Windows.Forms.Button();
            this.btRilevazioneTrimestrale = new System.Windows.Forms.Button();
            this.btVersion = new System.Windows.Forms.Button();
            this.txtLog = new System.Windows.Forms.TextBox();
            this.btAggiungiDatiAnamnestici = new System.Windows.Forms.Button();
            this.btEsenzioni = new System.Windows.Forms.Button();
            this.btComplicazioni = new System.Windows.Forms.Button();
            this.btAuthorizationToken = new System.Windows.Forms.Button();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.groupBox2 = new System.Windows.Forms.GroupBox();
            this.btFineRilevazione = new System.Windows.Forms.Button();
            this.btDatiStartup = new System.Windows.Forms.Button();
            this.btDatiAnnuali = new System.Windows.Forms.Button();
            this.btRilevazioneSemestrale = new System.Windows.Forms.Button();
            this.groupBox1.SuspendLayout();
            this.groupBox2.SuspendLayout();
            this.SuspendLayout();
            // 
            // btAggiungiPaziente
            // 
            this.btAggiungiPaziente.Location = new System.Drawing.Point(6, 19);
            this.btAggiungiPaziente.Name = "btAggiungiPaziente";
            this.btAggiungiPaziente.Size = new System.Drawing.Size(154, 35);
            this.btAggiungiPaziente.TabIndex = 0;
            this.btAggiungiPaziente.Text = "AggiungiPaziente";
            this.btAggiungiPaziente.UseVisualStyleBackColor = true;
            this.btAggiungiPaziente.Click += new System.EventHandler(this.btAggiungiPaziente_Click);
            // 
            // btRilevazioneTrimestrale
            // 
            this.btRilevazioneTrimestrale.Location = new System.Drawing.Point(6, 58);
            this.btRilevazioneTrimestrale.Name = "btRilevazioneTrimestrale";
            this.btRilevazioneTrimestrale.Size = new System.Drawing.Size(154, 35);
            this.btRilevazioneTrimestrale.TabIndex = 1;
            this.btRilevazioneTrimestrale.Text = "Aggiungi Dati Trimestrali";
            this.btRilevazioneTrimestrale.UseVisualStyleBackColor = true;
            this.btRilevazioneTrimestrale.Click += new System.EventHandler(this.btRilevazioneTrimestrale_Click);
            // 
            // btVersion
            // 
            this.btVersion.Location = new System.Drawing.Point(18, 13);
            this.btVersion.Name = "btVersion";
            this.btVersion.Size = new System.Drawing.Size(154, 35);
            this.btVersion.TabIndex = 2;
            this.btVersion.Text = "Check Version";
            this.btVersion.UseVisualStyleBackColor = true;
            this.btVersion.Click += new System.EventHandler(this.btVersion_Click);
            // 
            // txtLog
            // 
            this.txtLog.Location = new System.Drawing.Point(214, 13);
            this.txtLog.Multiline = true;
            this.txtLog.Name = "txtLog";
            this.txtLog.Size = new System.Drawing.Size(389, 553);
            this.txtLog.TabIndex = 3;
            // 
            // btAggiungiDatiAnamnestici
            // 
            this.btAggiungiDatiAnamnestici.Location = new System.Drawing.Point(6, 60);
            this.btAggiungiDatiAnamnestici.Name = "btAggiungiDatiAnamnestici";
            this.btAggiungiDatiAnamnestici.Size = new System.Drawing.Size(154, 35);
            this.btAggiungiDatiAnamnestici.TabIndex = 4;
            this.btAggiungiDatiAnamnestici.Text = "Aggiungi Dati Anamnestici";
            this.btAggiungiDatiAnamnestici.UseVisualStyleBackColor = true;
            this.btAggiungiDatiAnamnestici.Click += new System.EventHandler(this.btAggiungiDatiAnamnestici_Click);
            // 
            // btEsenzioni
            // 
            this.btEsenzioni.Location = new System.Drawing.Point(6, 101);
            this.btEsenzioni.Name = "btEsenzioni";
            this.btEsenzioni.Size = new System.Drawing.Size(154, 35);
            this.btEsenzioni.TabIndex = 5;
            this.btEsenzioni.Text = "Aggiungi Esenzioni";
            this.btEsenzioni.UseVisualStyleBackColor = true;
            this.btEsenzioni.Click += new System.EventHandler(this.btEsenzioni_Click);
            // 
            // btComplicazioni
            // 
            this.btComplicazioni.Location = new System.Drawing.Point(6, 142);
            this.btComplicazioni.Name = "btComplicazioni";
            this.btComplicazioni.Size = new System.Drawing.Size(154, 35);
            this.btComplicazioni.TabIndex = 6;
            this.btComplicazioni.Text = "Aggiungi Complicazioni";
            this.btComplicazioni.UseVisualStyleBackColor = true;
            this.btComplicazioni.Click += new System.EventHandler(this.btComplicazioni_Click);
            // 
            // btAuthorizationToken
            // 
            this.btAuthorizationToken.Location = new System.Drawing.Point(18, 63);
            this.btAuthorizationToken.Name = "btAuthorizationToken";
            this.btAuthorizationToken.Size = new System.Drawing.Size(154, 35);
            this.btAuthorizationToken.TabIndex = 7;
            this.btAuthorizationToken.Text = "GetAuthorization Token";
            this.btAuthorizationToken.UseVisualStyleBackColor = true;
            this.btAuthorizationToken.Click += new System.EventHandler(this.btAuthorizationToken_Click);
            // 
            // groupBox1
            // 
            this.groupBox1.Controls.Add(this.btEsenzioni);
            this.groupBox1.Controls.Add(this.btAggiungiPaziente);
            this.groupBox1.Controls.Add(this.btAggiungiDatiAnamnestici);
            this.groupBox1.Controls.Add(this.btComplicazioni);
            this.groupBox1.Location = new System.Drawing.Point(12, 122);
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size(166, 185);
            this.groupBox1.TabIndex = 8;
            this.groupBox1.TabStop = false;
            this.groupBox1.Text = "Dati Paziente";
            // 
            // groupBox2
            // 
            this.groupBox2.Controls.Add(this.btFineRilevazione);
            this.groupBox2.Controls.Add(this.btDatiStartup);
            this.groupBox2.Controls.Add(this.btDatiAnnuali);
            this.groupBox2.Controls.Add(this.btRilevazioneSemestrale);
            this.groupBox2.Controls.Add(this.btRilevazioneTrimestrale);
            this.groupBox2.Location = new System.Drawing.Point(12, 343);
            this.groupBox2.Name = "groupBox2";
            this.groupBox2.Size = new System.Drawing.Size(167, 223);
            this.groupBox2.TabIndex = 9;
            this.groupBox2.TabStop = false;
            this.groupBox2.Text = "Dati Rilevazione";
            // 
            // btFineRilevazione
            // 
            this.btFineRilevazione.Location = new System.Drawing.Point(7, 181);
            this.btFineRilevazione.Name = "btFineRilevazione";
            this.btFineRilevazione.Size = new System.Drawing.Size(154, 35);
            this.btFineRilevazione.TabIndex = 5;
            this.btFineRilevazione.Text = "Fine Rilevazione";
            this.btFineRilevazione.UseVisualStyleBackColor = true;
            this.btFineRilevazione.Click += new System.EventHandler(this.btFineRilevazione_Click);
            // 
            // btDatiStartup
            // 
            this.btDatiStartup.Location = new System.Drawing.Point(7, 19);
            this.btDatiStartup.Name = "btDatiStartup";
            this.btDatiStartup.Size = new System.Drawing.Size(154, 35);
            this.btDatiStartup.TabIndex = 4;
            this.btDatiStartup.Text = "Aggiungi Dati Startup";
            this.btDatiStartup.UseVisualStyleBackColor = true;
            this.btDatiStartup.Click += new System.EventHandler(this.btDatiStartup_Click);
            // 
            // btDatiAnnuali
            // 
            this.btDatiAnnuali.Location = new System.Drawing.Point(6, 140);
            this.btDatiAnnuali.Name = "btDatiAnnuali";
            this.btDatiAnnuali.Size = new System.Drawing.Size(154, 35);
            this.btDatiAnnuali.TabIndex = 3;
            this.btDatiAnnuali.Text = "Aggiungi Dati Annuali";
            this.btDatiAnnuali.UseVisualStyleBackColor = true;
            this.btDatiAnnuali.Click += new System.EventHandler(this.btDatiAnnuali_Click);
            // 
            // btRilevazioneSemestrale
            // 
            this.btRilevazioneSemestrale.Location = new System.Drawing.Point(6, 99);
            this.btRilevazioneSemestrale.Name = "btRilevazioneSemestrale";
            this.btRilevazioneSemestrale.Size = new System.Drawing.Size(154, 35);
            this.btRilevazioneSemestrale.TabIndex = 2;
            this.btRilevazioneSemestrale.Text = "Aggiungi Dati Semestrali";
            this.btRilevazioneSemestrale.UseVisualStyleBackColor = true;
            this.btRilevazioneSemestrale.Click += new System.EventHandler(this.btRilevazioneSemestrale_Click);
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(616, 572);
            this.Controls.Add(this.groupBox2);
            this.Controls.Add(this.groupBox1);
            this.Controls.Add(this.btAuthorizationToken);
            this.Controls.Add(this.txtLog);
            this.Controls.Add(this.btVersion);
            this.Name = "Form1";
            this.Text = "Client di test per il progetto AIR";
            this.groupBox1.ResumeLayout(false);
            this.groupBox2.ResumeLayout(false);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button btAggiungiPaziente;
        private System.Windows.Forms.Button btRilevazioneTrimestrale;
        private System.Windows.Forms.Button btVersion;
        private System.Windows.Forms.TextBox txtLog;
        private System.Windows.Forms.Button btAggiungiDatiAnamnestici;
        private System.Windows.Forms.Button btEsenzioni;
        private System.Windows.Forms.Button btComplicazioni;
        private System.Windows.Forms.Button btAuthorizationToken;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.GroupBox groupBox2;
        private System.Windows.Forms.Button btDatiStartup;
        private System.Windows.Forms.Button btDatiAnnuali;
        private System.Windows.Forms.Button btRilevazioneSemestrale;
        private System.Windows.Forms.Button btFineRilevazione;
    }
}

