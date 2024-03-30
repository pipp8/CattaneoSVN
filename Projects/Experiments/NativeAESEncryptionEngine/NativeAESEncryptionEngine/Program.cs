using System;
using System.Text;
using System.IO;
using System.Security.Cryptography;
using System.Collections.Generic;
using System.Linq;  // per SequenceEqual


namespace NativeAESEncryptionEngine
{
    class AESEngine
    {

        private byte[] key;
        private byte[] iv;
        public const int KeyLength = 128;
        private AesManaged engine;

        public int AESLength
        { 
            get { return this.engine.KeySize;}
        }

        public byte[] InitVector
        {
            get { return this.iv; }
            set { this.iv = value;}
        }

        public byte[] AESKey 
        { 
            get { return this.key; }
            set { this.key = value; }
        }

        public AESEngine()
		{
            this.key = null;
            this.iv = null;  // inizializzati dalla Init() o dalle proprietà

            this.engine = new AesManaged();
            this.engine.Mode = CipherMode.CBC;
            this.engine.Padding = PaddingMode.PKCS7;
            this.engine.KeySize = KeyLength;
            this.engine.BlockSize = KeyLength;
		}


        // DEVE essere chiamata SOLO una volta prima di qualsiasi Encrypt o Decrypt
        public void Init(byte[] iv, string key)
        {
            if (iv == null)
            {
                // il vettore di inizializzazione è generato (casualmente) dalla classe AESManaged
                this.iv = engine.IV;
            }
            else
            {
                this.iv = iv;
            }

            SHA512 sha = new SHA512CryptoServiceProvider(); 
            byte[] hash = sha.ComputeHash(Encoding.UTF8.GetBytes(key));    // per essere certi della lunghezza della chiave
            this.key = new byte[KeyLength / 8];
            System.Buffer.BlockCopy(hash, 0, this.key, 0, KeyLength/8);
        }

        public void setKey(string key)
        {
            SHA512 sha = new SHA512CryptoServiceProvider();
            byte[] hash = sha.ComputeHash(Encoding.UTF8.GetBytes(key));    // per essere certi della lunghezza della chiave
            this.key = new byte[KeyLength / 8];
            System.Buffer.BlockCopy(hash, 0, this.key, 0, KeyLength / 8);
        }


        // Cifra un byte array con AES 128 (altre chiavi possono essere configurate
        // "key" è la chiave e deve essere di lunghezza >= 128 bit key
        // "plainText" è un byte array che deve essere cifrato
        //
        // Ritorna un byte array con il risultato della cifratura
        //
        public byte[] Encrypt(byte[] plainText)
        {
            using (MemoryStream ms = new MemoryStream())
            {
                if (iv == null || key == null)
                {
                    // raise();
                    return null;
                }
                else 
                {
                    using (CryptoStream cs = new CryptoStream(ms, engine.CreateEncryptor(this.key, this.iv), CryptoStreamMode.Write))
                    {
                        cs.Write(plainText, 0, plainText.Length);
                    }

                    // byte[] cypherText = ms.ToArray();

                    ////Create new byte array that should contain both unencrypted iv and encrypted data
                    //byte[] result = new byte[iv.Length + cypherText.Length];

                    ////copy our 2 array into one
                    //System.Buffer.BlockCopy(iv, 0, result, 0, iv.Length);
                    //System.Buffer.BlockCopy(cypherText, 0, result, iv.Length, cypherText.Length);

                    return ms.ToArray();
                }
            }
        }


        // Decifra un byte array utilizzando AES 128 (altre chiavi possono essere configurate)
        // "key" è la chiave e deve essere di lunghezza >= 128 bit key
        // "cypherText" è un byte array che deve essere decifrato
        //
        // Ritorna un byte array con il testo decifrato (plainText)
        //
        public byte[] Decrypt(byte[] cypherText)
        {
            using (MemoryStream ms = new MemoryStream())
            {
                using (CryptoStream cs = new CryptoStream(ms, this.engine.CreateDecryptor(this.key, this.iv), CryptoStreamMode.Write))
                {
                    cs.Write(cypherText, 0, cypherText.Length);
                }
                return ms.ToArray();
            }
        }


        static void Main(string[] args)
        {
            AESEngine eng = new AESEngine();

            eng.Init( null, "pippo");                           // genera un initialization vector random e deriva la chiave dalla stringa "pippo"

            string input = "inòàòà!(!!(&(/!&!(/!(&!/&!(!/FJFGJHF°°ççç€sdfsd  sdf sdfs fsf sdf sf sdf sdf";
            byte[] plain = Encoding.UTF8.GetBytes(input);

            byte[] cypher = eng.Encrypt(plain);                 // per cifrare una stringa (Modalità CBC => l'output cypherText sarà di lunghezza multipla di 16 byte)

            byte[] plain2 = eng.Decrypt(cypher);                // per decifrare una testo cifrato 

            if (!plain.SequenceEqual(plain2))
            {
                Console.WriteLine("Plain diverso da plain2");
            }
            else
            {
                Console.WriteLine("byte arrays uguali");
            }

            string input2 = System.Text.Encoding.UTF8.GetString(plain2, 0, plain2.Length);

            if (input.CompareTo(input2) != 0)
            {
                Console.WriteLine("input diverso da input2");
            }
            else
            {
                Console.WriteLine("input string uguali");
            }
        }
    }

}
