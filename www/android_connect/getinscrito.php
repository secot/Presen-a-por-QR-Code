<?php

$response = array();

require_once __DIR__ . '/db_connect.php';

$db = new Db();
$db_con = $db -> connect();

if (isset($_GET["id"])) {
    $inscrito_id = $_GET['id'];

    $result = mysqli_query($db_con, "SELECT * FROM users WHERE id = $inscrito_id");

    if (!empty($result)) {
        if (mysqli_num_rows($result) > 0) {

            $result = mysqli_fetch_array($result);

            $pessoa = array();
            $pessoa["id"] = $result["id"];
            $pessoa["nome"] = $result["name"];
            $pessoa["qrid"] = $result["id_qr"];

            $response["success"] = 1;

            $response["pessoa"] = array();

            array_push($response["pessoa"], $pessoa);

            echo json_encode($response);
        } else {
            $response["success"] = 0;
            $response["message"] = "Pessoa não encontrada";

            echo json_encode($response);
        }
    } else {
        $response["success"] = 0;
        $response["message"] = "Pessoa não encontrada";

        echo json_encode($response);
    }
} else {
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";

    echo json_encode($response);
}
?>
