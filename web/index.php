<?php
$query = isset($_POST['query']) ? $_POST['query'] : '';
if(empty($query)){
}else{
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

      break;
    }else{
      sleep(1);
    }
  }
}
?>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>ICS Search System</title>

    <!-- Bootstrap -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
  </head>
<body>
  <div class="container">
    <div class="row">
      <div class="col-md-4 col-md-offset-4">
        <center>
          <h1>ICS @ UCI Search</h1>
        </center>
      </div>
    </div>
    <div class="row">
      <div class="col-md-6 col-md-offset-3">
        <center>
          <form method="post" action="index.php">
            <input type="text" name="query" class="form-control input-lg" placeholder="Search..." size="60" style="margin-bottom:10px;">
            <input type="submit" class="btn btn-primary" value="Search">
          </form>
        </center>
      </div>
    </div>
    <?php
    if(!empty($search_result)){
    ?>
    <div class="row">
      <div class="col-md-8 col-md-offset-2">
        <?php
        echo $search_result;
        ?>
      </div>
    </div>
    <?php
    }
    ?>
  </div>

  <!-- Bootstrap -->
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
  <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/js/bootstrap.min.js"></script>
</body>
</html>