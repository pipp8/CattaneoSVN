
use Win32::PerfLib;
my $server = "sa03wk053";
my $object = "Server";
my $object = "System";

Win32::PerfLib::GetCounterNames($server, \%counter);
%r_counter = map { $counter{$_} => $_ } keys %counter;

foreach $i (keys %r_counter) {
  printf("counter obj: %s -> id: %s\n", $i, $r_counter{$i});
}
printf("--------------------------------\n\n");

# retrieve the id for selected object
$counter_obj = $r_counter{$object};

# create connection to $server
$perflib = new Win32::PerfLib($server);
$proc_ref = {};
# get the performance data for the process object
$perflib->GetObjectList($counter_obj, $proc_ref);
$perflib->Close();
$instance_ref = $proc_ref->{Objects}->{$counter_obj}->{Instances};

@AllInstances = sort keys %{$instance_ref};
if ($#AllInstances > 0)   {
  foreach $p (@AllInstances)  {
    $counter_ref = $instance_ref->{$p}->{Counters};

    foreach $i (keys %{$counter_ref}) {
      printf("CN -> %s: %6d (%s) (name: %s)\n", 
	     $counter{ $counter_ref->{$i}->{CounterNameTitleIndex}},
	     $counter_ref->{$i}->{Counter},
	     Win32::PerfLib::GetCounterType($counter_ref->{$i}->{CounterType}),
	     $instance_ref->{$p}->{Name});
    }
  }
}
else   {
  $counters_ref = $proc_ref->{Objects}->{$counter_obj}->{Counters};
  @AllCounters = sort keys %{$counters_ref};
  printf("%d instances %d counters\n", $#AllInstance_ref, $#AllCounters);

  if ($#AllCounters > 0)  {
    foreach $i (@AllCounters)  {
      $final_ref = $counters_ref->{$i};
      printf("CN %d -> %s: %6d (%s)\n", $i, 
	     $counter{ $final_ref->{CounterNameTitleIndex}},
	     $final_ref->{Counter},
	     Win32::PerfLib::GetCounterType($final_ref->{CounterType}));
    }
  }
}

