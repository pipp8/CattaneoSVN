using System;
using System.Runtime.Remoting.Channels.Ipc;
using System.Security.Permissions;
using System.Diagnostics;
using SharedObjectLibrary;


namespace SampleClient
{
    public class Client
    {
        [SecurityPermission(SecurityAction.Demand)]
        public static void Main(string[] args)
        {
            // Create the channel.
            IpcChannel channel = new IpcChannel();

            // Register the channel.
            System.Runtime.Remoting.Channels.ChannelServices.RegisterChannel(channel);

            // Register as client for remote object.
            System.Runtime.Remoting.WellKnownClientTypeEntry remoteType =
                new System.Runtime.Remoting.WellKnownClientTypeEntry(
                    typeof(RemoteObject), "ipc://localhost:9090/RemoteObject.rem");

            System.Runtime.Remoting.RemotingConfiguration.RegisterWellKnownClientType(remoteType);

            // Create a message sink.
            string objectUri;
            System.Runtime.Remoting.Messaging.IMessageSink messageSink =
                channel.CreateMessageSink( "ipc://localhost:9090/RemoteObject.rem", null, out objectUri);

            Console.WriteLine("The URI of the message sink is {0}.", objectUri);

            if (messageSink != null)
            {
                Console.WriteLine("The type of the message sink is {0}.",
                    messageSink.GetType().ToString());
            }

            // Create an instance of the remote object.
            RemoteObject service = new RemoteObject();

            // Invoke a method on the remote object.
            Console.WriteLine("The client is invoking the remote object.");
            Console.WriteLine("The remote object has been called {0} times.",
                service.GetCount());

            int repeat = 100000;
            Random rng = new Random();

            double op1, op2, ris;

            Stopwatch stopWatch = new Stopwatch();
            stopWatch.Start();

            for (int i = 0; i < repeat; i++)
            {
                op1 = rng.NextDouble();
                op2 = rng.NextDouble();

                ris = service.Add(op1, op2);
                Console.WriteLine("Call Add method {0} + {1} = {2}", op1, op2, ris);
            }
            stopWatch.Stop();
            // Get the elapsed time as a TimeSpan value.

            TimeSpan ts = stopWatch.Elapsed;

            // Format and display the TimeSpan value.

            string elapsedTime = String.Format("{0:00}:{1:00}:{2:00}.{3:00}",
              ts.Hours, ts.Minutes, ts.Seconds, ts.Milliseconds / 10);

            Console.WriteLine("Get FloatProperty = {0}", service.DoubleProperty);
            Console.WriteLine("Total time span for {0} calls = {1}", repeat, elapsedTime);
        }
    }

}