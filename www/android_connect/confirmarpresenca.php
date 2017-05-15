<?php

$response = array();

if (isset($_GET['id_pessoa']) && isset($_GET['id_palestra']) && isset($_GET['presente'])) {

    $id_pessoa = $_GET['id_pessoa'];
	  $id_palestra = $_GET['id_palestra'];
    $presente = $_GET['presente'];

    require_once __DIR__ . '/db_connect.php';

    $db = new Db();
	  $db_con = $db->connect();

    $inscrito = mysqli_query($db_con, "SELECT presenca from interesses where id_pessoa = $id_pessoa and id_palestra = $id_palestra");
    $inscrito_count = $inscrito->num_rows;

    if ($inscrito_count > 0)
      $result = mysqli_query($db_con, "UPDATE interesses SET presenca = '$presente' WHERE id_pessoa = $id_pessoa AND id_palestra = $id_palestra");
    else
      $result = mysqli_query($db_con, "INSERT INTO `interesses`(`id_pessoa`, `id_palestra`, `presenca`, `created_at`, `updated_at`) values ('". $id_pessoa . "', '". $id_palestra . "', '". $presente . "', (select now()), (select now()) )");

	if ($db_con -> affected_rows == 0 ) {
		$response["success"] = 0;
        $response["message"] = "Presenca não atualizada";

		echo json_encode($response);
	} else if ($result) {
        $response["success"] = 1;
        $response["message"] = "$id_pessoa:$presente";

        echo json_encode($response);
    } else {
		$response["success"] = 0;
        $response["message"] = "Presenca não atualizada";

		echo json_encode($response);
    }
} else {
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";

    echo json_encode($response);
}
?>
