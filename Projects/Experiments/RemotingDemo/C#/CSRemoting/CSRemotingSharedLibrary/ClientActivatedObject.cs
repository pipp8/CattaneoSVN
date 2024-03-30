/****************************** Module Header ******************************\
Module Name:  ClientActivatedObject.cs
Project:      CSRemotingSharedLibrary
Copyright (c) Microsoft Corporation.

ClientActivatedObject.cs defines a client-activated type for .NET Remoting.
Client-activated objects are created by the server and their lifetime is 
managed by the client. In contrast to server-activated objects, client-
activated objects are created as soon as the client calls "new" or any 
other object creation methods. Client-activated objects are specific to the 
client, and objects are not shared among different clients; object instance 
exists until the lease expires or the client destroys the object. 

This source is subject to the Microsoft Public License.
See http://www.microsoft.com/en-us/openness/licenses.aspx#MPL.
All other rights reserved.

THIS CODE AND INFORMATION IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, 
EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A PARTICULAR PURPOSE.
\***************************************************************************/

using System;
using System.Runtime.InteropServices;
using System.Diagnostics;

namespace RemotingShared
{
    /// <summary>
    /// A client-activated type for .NET Remoting.
    /// </summary>
    public class ClientActivatedObject : MarshalByRefObject
    {
        /// <summary>
        /// A float property.
        /// </summary>
        public float FloatProperty
        {
            get;
            set;
        }

        /// <summary>
        /// Get the type of the remote object. 
        /// </summary>
        public virtual string GetRemoteObjectType()
        {
            return "ClientActivatedObject";
        }

        public double Add(double n1, double n2)
        {
            return ((double)(FloatProperty = (float)(n1 + n2)));
        }
        public double Subtract(double n1, double n2)
        {
            return n1 - n2;
        }
        public double Multiply(double n1, double n2)
        {
            return n1 * n2;
        }
        public double Divide(double n1, double n2)
        {
            return ((double) (FloatProperty = (float ) ( n1 / n2)));
        }


        /// <summary>
        /// Get the current process ID and thread ID.
        /// </summary>
        /// <param name="processId">current process ID</param>
        /// <param name="threadId">current thread ID</param>
        public void GetProcessThreadID(out uint processId, out uint threadId)
        {
            processId = (uint)Process.GetCurrentProcess().Id;
            threadId = GetCurrentThreadId();
        }

        /// <summary>
        /// Get the current thread ID.
        /// </summary>
        [DllImport("kernel32.dll")]
        internal static extern uint GetCurrentThreadId();
    }
}
