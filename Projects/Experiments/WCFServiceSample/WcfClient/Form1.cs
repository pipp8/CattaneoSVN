/* Step By Step Simple Sample Series
 * 
 * Example: WCF: Writing/consuming a service using VS2010
 * 
 * Scenario: 
 * Book store service(WCF) that would fetch the book information.
 * 
 * Technology:
 * WCF, Windows Forms, LINQ
 * 
 * 
 * Narrator: Kamran Khan
 * Blog: IZLOOITE.wordpress.com (aka izz-loo-aight)
 * 
 * Creative Commons -ShareAlike http://creativecommons.org/licenses/by-nc-sa/2.0/
 * 
 */

using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using WcfClient.SvcBookStore;
using System.Xml.Linq;

namespace WcfClient
{

    public partial class Form1 : Form
    {
        BookServiceClient bookService;//Declare service object
        public Form1()
        {
            InitializeComponent();
            bookService = new BookServiceClient();//Initialize
        }

        private void button1_Click(object sender, EventArgs e)
        {
            //Get the combo choice, if there is any.
            string strGenre = cbxGenre.SelectedIndex > -1 ? cbxGenre.SelectedItem.ToString() : string.Empty;

            //Declare the books array; though the actual return type is List<Books>, it actually gets casted into
            //Book[] array.
            Book[] lstBooks = null;

            //Discard other filters, if user has entered a book id
            if (!string.IsNullOrEmpty(txtID.Text))
            {
                lstBooks = bookService.GetBookByID(txtID.Text);
            }
            else
            {
                //Lets get books by filter.
                lstBooks = bookService.Filter(Author: txtAuthor.Text, Title: TxtTitle.Text, Genre: strGenre);
            }

            //Set datasource, custom object.
            dataGridView1.DataSource = lstBooks;

        }
    }
}
