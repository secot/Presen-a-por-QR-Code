<?php

$response = array();

require_once __DIR__ . '/db_connect.php';

$db = new Db();
$db_con = $db -> connect();

 if (isset($_GET["palestra_id"])) {
    $palestra_id = $_GET['palestra_id'];
	$result = mysqli_query($db_con, "SELECT users.id as id_pessoa, users.name as name, users.id_qr as id_qr, interesses.presenca as presenca FROM users INNER JOIN interesses ON users.id = interesses.id_pessoa WHERE interesses.id_palestra = $palestra_id ORDER BY users.name") or die(mysqli.err());

	if (mysqli_num_rows($result) > 0) {
		$response["pessoas"] = array();

		while ($row = mysqli_fetch_array($result)) {
			$pessoa = array();
			$pessoa["id"] = $row["id_pessoa"];
			$pessoa["nome"] = $row["name"];
			$pessoa["qrid"] = $row["id_qr"];
			$pessoa["presente"] = $row["presenca"];

			array_push($response["pessoas"], $pessoa);
		}
		$response["success"] = 1;

		echo json_encode($response);

	} else {
		$response["success"] = 0;
		$response["message"] = "Nenhuma pessoa encontrada";

		echo json_encode($response);
	}

} else {
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";

    echo json_encode($response);
}
?>
