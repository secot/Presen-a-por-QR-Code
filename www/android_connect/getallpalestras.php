<?php

$response = array();

require_once __DIR__ . '/db_connect.php';

$db = new Db();
$db_con = $db -> connect();

$result = mysqli_query($db_con, "SELECT * FROM palestras ORDER BY data") or die(mysqli.err());

if (mysqli_num_rows($result) > 0) {
    $response["palestras"] = array();

    while ($row = mysqli_fetch_array($result)) {
        $palestra = array();
        $palestra["id"] = $row["id"];
        $palestra["titulo"] = $row["titulo"];
        $palestra["palestrante"] = $row["palestrante"];

        array_push($response["palestras"], $palestra);
    }
    $response["success"] = 1;

    echo json_encode($response);

} else {
    $response["success"] = 0;
    $response["message"] = "Nenhuma palestra encontrada";

    echo json_encode($response);
}
?>
