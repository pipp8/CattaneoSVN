﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Security.Cryptography;


namespace TestAIRClient
{
    class Utils
    {

        static public string CalculateMD5Hash(string input)
        {
            // step 1, calculate MD5 hash from input
            MD5 md5 = System.Security.Cryptography.MD5.Create();
            byte[] inputBytes = System.Text.Encoding.ASCII.GetBytes(input);
            byte[] hash = md5.ComputeHash(inputBytes);

            return System.Convert.ToBase64String(hash);

            //// step 2, convert byte array to hex string
            //StringBuilder sb = new StringBuilder();
            //for (int i = 0; i < hash.Length; i++)
            //{
            //    sb.Append(hash[i].ToString("X2"));
            //}
            //return sb.ToString();
        }
    }
}
