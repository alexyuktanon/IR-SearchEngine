<?php
$query = $_POST["query"];

// echo "Searching in progress, please wait!";

//Write query file
$file = fopen("share/query.txt", "w") or die("Unable to open file!");
fwrite($file, $query);
fclose($file);

//Change process status
$file = fopen("share/status-q.txt", "w") or die("Unable to open file!");
fwrite($file, "1"); //0 = No changes, 1 = New query
fclose($file);

while(true){
  //Read result status
  $file = fopen("share/status-r.txt", "r") or die("Unable to open file!");
  $status_r = fread($file, filesize("share/status-r.txt"));
  fclose($file);

  if($status_r == "1"){
    //Change result status
    $file = fopen("share/status-r.txt", "w") or die("Unable to open file!");
    fwrite($file, "0"); //0 = Old result, 1 = New result
    fclose($file);

    $file = fopen("share/result.txt", "r") or die("Unable to open file!");
    $search_result = fread($file, filesize("share/result.txt"));
    fclose($file);

    echo $search_result;
    break;
  }else{
    sleep(1);
  }
}
?>