using System;
using System.Diagnostics;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using CalcClient.CalcServiceReference;

namespace CalcClient
{
    public partial class Form1 : Form
    {
        CalculatorClient svc;

        public Form1()
        {
            InitializeComponent();

            svc = new CalculatorClient();
        }

        private void btRun_Click(object sender, EventArgs e)
        {
            int ndx = cmbOpType.SelectedIndex + 1;
            Double ris;

            if (ndx == 0) {
                var result = MessageBox.Show("Occorre prima selazionare l'operazione richiesta", "Operation Error",
                                             MessageBoxButtons.OK, MessageBoxIcon.Error);
                return;
            }

            switch (ndx)
            {
                case 1: ris = svc.Add(Convert.ToDouble(txtOperando1.Text), Convert.ToDouble(txtOperando2.Text));
                    txtRisultato.Text = ris.ToString();
                    break;

                case 2: ris = svc.Subtract(Convert.ToDouble(txtOperando1.Text), Convert.ToDouble(txtOperando2.Text));
                    txtRisultato.Text = ris.ToString();
                    break;

                case 3: ris = svc.Multiply(Convert.ToDouble(txtOperando1.Text), Convert.ToDouble(txtOperando2.Text));
                    txtRisultato.Text = ris.ToString();
                    break;

                case 4: ris = svc.Divide(Convert.ToDouble(txtOperando1.Text), Convert.ToDouble(txtOperando2.Text));
                    txtRisultato.Text = ris.ToString();
                    break;

                case 5: txtRisultato.Text = svc.GetData(Convert.ToInt32(txtOperando1.Text));
                    break;

                case 6: 
                    CompositeType obj = new CompositeType();
                    obj.BoolValue = true;
                    obj.StringValue = "in";
                    CompositeType ret = svc.GetDataUsingDataContract(obj);

                    txtRisultato.Text = ret.StringValue + ret.BoolValue;
                    break;
            }
        }

        private void btMultipleCalls_Click(object sender, EventArgs e)
        {
            int repeat = Convert.ToInt32(txtOperando1.Text);
            Random rng = new Random();

            double op1, op2, ris;

            Stopwatch stopWatch = new Stopwatch();
            stopWatch.Start();

            for(int i = 0; i < repeat; i++) 
            {
                op1 = rng.NextDouble();
                op2 = rng.NextDouble();

                ris = svc.Add(op1, op2);
            }
            stopWatch.Stop();
            // Get the elapsed time as a TimeSpan value.
  
            TimeSpan ts = stopWatch.Elapsed;
  
            // Format and display the TimeSpan value.
  
            string elapsedTime = String.Format("{0:00}:{1:00}:{2:00}.{3:00}",
              ts.Hours, ts.Minutes, ts.Seconds, ts.Milliseconds / 10);

            txtRisultato.Text = elapsedTime;
        }
    }
}
