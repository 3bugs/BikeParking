<?php
require_once 'global.php';

error_reporting(E_ERROR | E_PARSE);
header('Content-type: application/json; charset=utf-8');

header('Expires: Sun, 01 Jan 2014 00:00:00 GMT');
header('Cache-Control: no-store, no-cache, must-revalidate');
header('Cache-Control: post-check=0, pre-check=0', FALSE);
header('Pragma: no-cache');

$response = array();

$request = explode('/', trim($_SERVER['PATH_INFO'], '/'));
$action = strtolower(array_shift($request));
$id = array_shift($request);

require_once 'db_config.php';
$db = new mysqli(DB_SERVER, DB_USER, DB_PASSWORD, DB_DATABASE);

if ($db->connect_errno) {
    $response[KEY_ERROR_CODE] = ERROR_CODE_ERROR;
    $response[KEY_ERROR_MESSAGE] = 'เกิดข้อผิดพลาดในการเชื่อมต่อฐานข้อมูล';
    $response[KEY_ERROR_MESSAGE_MORE] = $db->connect_error;
    echo json_encode($response);
    exit();
}
$db->set_charset("utf8");

//sleep(1); //todo:

define("TYPE_USER", 0);
define("TYPE_PROVIDER", 1);

switch ($action) {
    case 'login':
        doLogin(0);
        break;
    case 'login_provider':
        doLogin(1);
        break;
    case 'register':
        doRegister();
        break;
    case 'get_parking_place':
        doGetParkingPlace();
        break;
    case 'add_parking_place':
        doAddParkingPlace();
        break;
    case 'update_parking_place':
        doUpdateParkingPlace();
        break;
    case 'delete_parking_place':
        doDeleteParkingPlace();
        break;
    case 'add_booking':
        doAddBooking();
        break;
    case 'update_booking':
        doUpdateBooking();
        break;
    case 'get_slip':
        doGetSlip();
        break;
    case 'save_rating_user':
        doSaveRating(TYPE_USER);
        break;
    case 'save_rating_provider':
        doSaveRating(TYPE_PROVIDER);
        break;
    case 'update_firebase_token_user':
        doUpdateFirebaseToken(TYPE_USER);
        break;
    case 'update_firebase_token_provider':
        doUpdateFirebaseToken(TYPE_PROVIDER);
        break;
    default:
        $response[KEY_ERROR_CODE] = ERROR_CODE_ERROR;
        $response[KEY_ERROR_MESSAGE] = 'No action specified or invalid action.';
        $response[KEY_ERROR_MESSAGE_MORE] = '';
        break;
}

$db->close();
echo json_encode($response);
exit();

function doLogin($role)
{
    global $db, $response;

    $pid = $db->real_escape_string($_POST['pid']);
    $password = $db->real_escape_string($_POST['password']);

    if ($role == 0) {
        $table = 'user';
    } else {
        $table = 'provider';
    }

    $selectUserSql = "SELECT * FROM $table WHERE `pid` = '$pid' AND `password` = '$password'";

    $selectUserResult = $db->query($selectUserSql);
    if ($selectUserResult) {
        if ($selectUserResult->num_rows > 0) {
            $response[KEY_ERROR_CODE] = ERROR_CODE_SUCCESS;
            $response[KEY_ERROR_MESSAGE] = '';
            $response[KEY_ERROR_MESSAGE_MORE] = '';
            $response[KEY_LOGIN_SUCCESS] = TRUE;
            $response['user'] = fetchUser($selectUserResult);
        } else {
            $response[KEY_ERROR_CODE] = ERROR_CODE_SUCCESS;
            $response[KEY_ERROR_MESSAGE] = '';
            $response[KEY_ERROR_MESSAGE_MORE] = '';
            $response[KEY_LOGIN_SUCCESS] = FALSE;
        }
        $selectUserResult->close();
    } else {
        $response[KEY_ERROR_CODE] = ERROR_CODE_ERROR;
        $response[KEY_ERROR_MESSAGE] = 'เกิดข้อผิดพลาดในการอ่านข้อมูลบัญชีผู้ใช้';
        $errMessage = $db->error;
        $response[KEY_ERROR_MESSAGE_MORE] = "$errMessage\nSQL: $selectUserSql";
    }
}

function fetchUser($selectUserResult)
{
    $row = $selectUserResult->fetch_assoc();

    $user = array();
    $user['id'] = (int)$row['id'];
    $user['pid'] = $row['pid'];
    $user['first_name'] = $row['first_name'];
    $user['last_name'] = $row['last_name'];

    return $user;
}

function doRegister()
{
    global $db, $response;

    $role = (int)$db->real_escape_string($_POST['role']);
    $pid = $db->real_escape_string($_POST['pid']);
    $phone = $db->real_escape_string($_POST['phone']);
    $password = $db->real_escape_string($_POST['password']);
    $firstName = $db->real_escape_string($_POST['first_name']);
    $lastName = $db->real_escape_string($_POST['last_name']);

    $selectExistingUserSQL = "SELECT * FROM `user` WHERE `pid` = '$pid'";
    $selectExistingUserResult = $db->query($selectExistingUserSQL);
    if ($selectExistingUserResult->num_rows > 0) {
        $response[KEY_ERROR_CODE] = ERROR_CODE_ERROR;
        $response[KEY_ERROR_MESSAGE] = "สมัครสมาชิกไม่ได้ เนื่องจากมีเลขประชาชน '$pid' อยู่ในระบบแล้ว";
        $response[KEY_ERROR_MESSAGE_MORE] = '';
        $selectExistingUserResult->close();
        return;
    }
    $selectExistingUserResult->close();

    $table = '';
    if ($role == 0) {
        $table = 'user';
    } else {
        $table = 'provider';
    }

    $insertUserSql = "INSERT INTO `$table` (`pid`, `phone`, `password`, `first_name`, `last_name`) "
        . " VALUES ('$pid', '$phone', '$password', '$firstName', '$lastName')";
    $insertUserResult = $db->query($insertUserSql);
    if ($insertUserResult) {
        $insertId = $db->insert_id;
        $selectUserSql = "SELECT * FROM `user` WHERE `id` = $insertId";

        $selectUserResult = $db->query($selectUserSql);
        if ($selectUserResult) {
            $response[KEY_ERROR_CODE] = ERROR_CODE_SUCCESS;
            $response[KEY_ERROR_MESSAGE] = '';
            $response[KEY_ERROR_MESSAGE_MORE] = '';
            $response['user'] = fetchUser($selectUserResult);
        }
    } else {
        $response[KEY_ERROR_CODE] = ERROR_CODE_ERROR;
        $response[KEY_ERROR_MESSAGE] = 'เกิดข้อผิดพลาดในการบันทึกข้อมูล';
        $errMessage = $db->error;
        $response[KEY_ERROR_MESSAGE_MORE] = "$errMessage\nSQL: $insertUserSql";
    }
}

function doAddParkingPlace()
{
    global $db, $response;

    $placeName = $db->real_escape_string($_POST['place_name']);
    $district = $db->real_escape_string($_POST['district']);
    $lotCount = $db->real_escape_string($_POST['lot_count']);
    $fee = $db->real_escape_string($_POST['fee']);
    $remark = $db->real_escape_string($_POST['remark']);
    $latitude = $db->real_escape_string($_POST['latitude']);
    $longitude = $db->real_escape_string($_POST['longitude']);
    $providerId = $db->real_escape_string($_POST['provider_id']);

    $sql = "INSERT INTO `parking_place` (`place_name`, `latitude`, `longitude`, `district`, `provider_id`, `lot_count`, `fee`, `remark`) "
        . " VALUES ('$placeName', '$latitude', '$longitude', '$district', $providerId, $lotCount, $fee, '$remark')";
    $result = $db->query($sql);
    if ($result) {
        $response[KEY_ERROR_CODE] = ERROR_CODE_SUCCESS;
        $response[KEY_ERROR_MESSAGE] = 'บันทึกข้อมูลสำเร็จ';
        $response[KEY_ERROR_MESSAGE_MORE] = '';
    } else {
        $response[KEY_ERROR_CODE] = ERROR_CODE_ERROR;
        $response[KEY_ERROR_MESSAGE] = 'เกิดข้อผิดพลาดในการบันทึกข้อมูล';
        $errMessage = $db->error;
        $response[KEY_ERROR_MESSAGE_MORE] = "$errMessage\nSQL: $sql";
    }
}

function doUpdateParkingPlace()
{
    global $db, $response;

    $placeName = $db->real_escape_string($_POST['place_name']);
    $district = $db->real_escape_string($_POST['district']);
    $lotCount = $db->real_escape_string($_POST['lot_count']);
    $fee = $db->real_escape_string($_POST['fee']);
    $remark = $db->real_escape_string($_POST['remark']);
    $latitude = $db->real_escape_string($_POST['latitude']);
    $longitude = $db->real_escape_string($_POST['longitude']);
    $parkingPlaceId = $db->real_escape_string($_POST['parking_place_id']);

    $sql = "UPDATE `parking_place` SET `place_name`='$placeName', `latitude`='$latitude', `longitude`='$longitude', "
        . " `district`='$district', `lot_count`=$lotCount, `fee`=$fee, `remark`='$remark' "
        . " WHERE id=$parkingPlaceId";
    $result = $db->query($sql);
    if ($result) {
        $response[KEY_ERROR_CODE] = ERROR_CODE_SUCCESS;
        $response[KEY_ERROR_MESSAGE] = 'บันทึกข้อมูลสำเร็จ';
        $response[KEY_ERROR_MESSAGE_MORE] = '';
    } else {
        $response[KEY_ERROR_CODE] = ERROR_CODE_ERROR;
        $response[KEY_ERROR_MESSAGE] = 'เกิดข้อผิดพลาดในการบันทึกข้อมูล';
        $errMessage = $db->error;
        $response[KEY_ERROR_MESSAGE_MORE] = "$errMessage\nSQL: $sql";
    }
}

function doDeleteParkingPlace()
{
    global $db, $response;

    $parkingPlaceId = $db->real_escape_string($_POST['parking_place_id']);

    $sql = "DELETE FROM `parking_place` WHERE id=$parkingPlaceId";
    $result = $db->query($sql);
    if ($result) {
        $response[KEY_ERROR_CODE] = ERROR_CODE_SUCCESS;
        $response[KEY_ERROR_MESSAGE] = 'ลบข้อมูลสำเร็จ';
        $response[KEY_ERROR_MESSAGE_MORE] = '';
    } else {
        $response[KEY_ERROR_CODE] = ERROR_CODE_ERROR;
        $response[KEY_ERROR_MESSAGE] = 'เกิดข้อผิดพลาดในการลบข้อมูล';
        $errMessage = $db->error;
        $response[KEY_ERROR_MESSAGE_MORE] = "$errMessage\nSQL: $sql";
    }
}

function doUpdateProfileImage($userType)
{
    global $db, $response;

    $table = '';
    if ($userType == USER_TYPE_ELDERLY) {
        $table = 'elderly';
    } else if ($userType == USER_TYPE_EMPLOYER) {
        $table = 'employer';
    }

    $userId = $_POST['user_id'];

    $imageFilename = createRandomString(16) . '.jpg';
    $dest = '../images/user/' . $imageFilename;

    if (!moveUploadedFile('profile_image', $dest)) {
        $response[KEY_ERROR_CODE] = 9; //ERROR_CODE_FILE_UPLOAD_ERROR;
        $response[KEY_ERROR_MESSAGE] = "เกิดข้อผิดพลาดในการบันทึกไฟล์รูปภาพ";
        $response[KEY_ERROR_MESSAGE_MORE] = getUploadErrorMessage($_FILES['profile_image']['error']);
    } else {
        $updateUserSql = "UPDATE `$table` SET image = '$imageFilename' WHERE id = $userId ";
        $updateUserResult = $db->query($updateUserSql);
        if ($updateUserResult) {
            $selectUserSql = "SELECT * FROM `$table` WHERE id = $userId ";
            $selectUserResult = $db->query($selectUserSql);

            if ($selectUserResult) {
                $response[KEY_ERROR_CODE] = ERROR_CODE_SUCCESS;
                $response[KEY_ERROR_MESSAGE] = '';
                $response[KEY_ERROR_MESSAGE_MORE] = '';
                $response['user'] = fetchUser($userType, $selectUserResult);
            } else {
                $response[KEY_ERROR_CODE] = ERROR_CODE_SQL_ERROR;
                $response[KEY_ERROR_MESSAGE] = 'เกิดข้อผิดพลาดในการเชื่อมต่อฐานข้อมูล';
                $errMessage = $db->error;
                $response[KEY_ERROR_MESSAGE_MORE] = "$errMessage\nSQL: $selectUserSql";
            }
        } else {
            $response[KEY_ERROR_CODE] = ERROR_CODE_SQL_ERROR;
            $response[KEY_ERROR_MESSAGE] = 'เกิดข้อผิดพลาดในการเชื่อมต่อฐานข้อมูล';
            $errMessage = $db->error;
            $response[KEY_ERROR_MESSAGE_MORE] = "$errMessage\nSQL: $updateUserSql";
        }
    }
}

function createRandomString($length)
{
    $key = '';
    $keys = array_merge(range(0, 9), range('a', 'z'));

    for ($i = 0; $i < $length; $i++) {
        $key .= $keys[array_rand($keys)];
    }

    return $key;
}

function moveUploadedFile($key, $dest)
{
    global $response;

    $response['name'] = $_FILES[$key]['name'];
    $response['type'] = $_FILES[$key]['type'];
    $response['size'] = $_FILES[$key]['size'];
    $response['tmp_name'] = $_FILES[$key]['tmp_name'];

    $src = $_FILES[$key]['tmp_name'];
    $response['upload_src'] = $src;

    $response['upload_dest'] = $dest;

    return move_uploaded_file($src, $dest);
}

function getUploadErrorMessage($errCode)
{
    $message = '';
    switch ($errCode) {
        case UPLOAD_ERR_OK:
            break;
        case UPLOAD_ERR_INI_SIZE:
        case UPLOAD_ERR_FORM_SIZE:
            $message .= 'File too large (limit of ' . get_max_upload() . ' bytes).';
            break;
        case UPLOAD_ERR_PARTIAL:
            $message .= 'File upload was not completed.';
            break;
        case UPLOAD_ERR_NO_FILE:
            $message .= 'Zero-length file uploaded.';
            break;
        default:
            $message .= 'Internal error #' . $errCode;
            break;
    }
    return $message;
}

function doGetParkingPlace()
{
    global $db, $response;

    $where = '';
    if (isset($_POST['provider_id'])) {
        $providerId = $db->real_escape_string($_POST['provider_id']);
        $where = " WHERE pp.provider_id = $providerId";
    }

    $sql = "SELECT pp.id AS id, pp.place_name, pp.latitude, pp.longitude, pp.district, pp.provider_id, pp.lot_count, pp.fee, pp.remark, p.first_name, p.last_name, p.firebase_token "
        . " FROM `parking_place` pp INNER JOIN `provider` p "
        . " ON pp.provider_id = p.id $where";
    if ($result = $db->query($sql)) {
        $response[KEY_ERROR_CODE] = ERROR_CODE_SUCCESS;
        $response[KEY_ERROR_MESSAGE] = 'อ่านข้อมูลสำเร็จ';
        $response[KEY_ERROR_MESSAGE_MORE] = '';

        $parkingPlaceList = array();
        while ($row = $result->fetch_assoc()) {
            $parkingPlace = array();
            $parkingPlaceId = (int)$row['id'];
            $parkingPlace['id'] = $parkingPlaceId;
            $parkingPlace['place_name'] = $row['place_name'];
            $parkingPlace['latitude'] = $row['latitude'];
            $parkingPlace['longitude'] = $row['longitude'];
            $parkingPlace['district'] = $row['district'];
            $parkingPlace['provider_id'] = (int)$row['provider_id'];
            $parkingPlace['lot_count'] = (int)$row['lot_count'];
            $parkingPlace['fee'] = (int)$row['fee'];
            $parkingPlace['remark'] = $row['remark'];
            $parkingPlace['first_name'] = $row['first_name'];
            $parkingPlace['last_name'] = $row['last_name'];
            $parkingPlace['firebase_token'] = $row['firebase_token'];
            $bookingList = array();

            $sql = "SELECT b.id AS id, b.user_id, b.parking_place_id, b.book_date, b.status, u.first_name AS user_first_name, u.last_name AS user_last_name, u.pid AS user_pid, u.phone AS user_phone, u.firebase_token AS user_firebase_token "
                . " FROM booking b INNER JOIN user u ON b.user_id = u.id "
                . " WHERE parking_place_id = $parkingPlaceId "
                . " ORDER BY b.book_date DESC";
            if ($bookingResult = $db->query($sql)) {
                while ($bookingRow = $bookingResult->fetch_assoc()) {
                    $booking = array();
                    $booking['id'] = (int)$bookingRow['id'];
                    $booking['user_id'] = (int)$bookingRow['user_id'];
                    $booking['user_first_name'] = $bookingRow['user_first_name'];
                    $booking['user_last_name'] = $bookingRow['user_last_name'];
                    $booking['user_pid'] = $bookingRow['user_pid'];
                    $booking['user_phone'] = $bookingRow['user_phone'];
                    $booking['user_firebase_token'] = $bookingRow['user_firebase_token'];
                    $booking['parking_place_id'] = (int)$bookingRow['parking_place_id'];
                    $booking['book_date'] = $bookingRow['book_date'];
                    $booking['status'] = (int)$bookingRow['status'];

                    array_push($bookingList, $booking);
                }
                $parkingPlace['booking_list'] = $bookingList;
                $bookingResult->close();
            } else {
                $response[KEY_ERROR_CODE] = ERROR_CODE_ERROR;
                $response[KEY_ERROR_MESSAGE] = 'เกิดข้อผิดพลาดในการอ่านข้อมูล';
                $errMessage = $db->error;
                $response[KEY_ERROR_MESSAGE_MORE] = "$errMessage\nSQL: $sql";

                $result->close();
                return;
            }

            array_push($parkingPlaceList, $parkingPlace);
        }
        $response['data_list'] = $parkingPlaceList;
        $result->close();
    } else {
        $response[KEY_ERROR_CODE] = ERROR_CODE_ERROR;
        $response[KEY_ERROR_MESSAGE] = 'เกิดข้อผิดพลาดในการอ่านข้อมูล';
        $errMessage = $db->error;
        $response[KEY_ERROR_MESSAGE_MORE] = "$errMessage\nSQL: $sql";
    }
}

function doAddBooking()
{
    global $db, $response;

    $userId = $db->real_escape_string($_POST['user_id']);
    $parkingPlaceId = $db->real_escape_string($_POST['parking_place_id']);

    $sql = "SELECT * FROM booking WHERE parking_place_id = $parkingPlaceId AND user_id = $userId AND status = 0";
    $result = $db->query($sql);
    if ($result) {
        if ($result->num_rows > 0) {
            $response[KEY_ERROR_CODE] = ERROR_CODE_ERROR;
            $response[KEY_ERROR_MESSAGE] = 'คุณได้จองที่จอดนี้แล้ว';
            $response[KEY_ERROR_MESSAGE_MORE] = '';
            $result->close();
            return;
        }
        $result->close();
    } else {
        $response[KEY_ERROR_CODE] = ERROR_CODE_ERROR;
        $response[KEY_ERROR_MESSAGE] = 'เกิดข้อผิดพลาดในการบันทึกข้อมูล';
        $errMessage = $db->error;
        $response[KEY_ERROR_MESSAGE_MORE] = "$errMessage\nSQL: $sql";
    }

    $sql = "SELECT pp.lot_count FROM parking_place pp INNER JOIN booking b ON pp.id = b.parking_place_id "
        . " WHERE parking_place_id = $parkingPlaceId AND status <> 2";
    $result = $db->query($sql);
    if ($result) {
        $numRows = $result->num_rows;
        if ($numRows > 0) {
            $row = $result->fetch_assoc();
            $lotCount = $row['lot_count'];
            if ($numRows >= $lotCount) {
                $response[KEY_ERROR_CODE] = ERROR_CODE_ERROR;
                $response[KEY_ERROR_MESSAGE] = 'ที่จอดเต็มแล้ว';
                $response[KEY_ERROR_MESSAGE_MORE] = '';
                $result->close();
                return;
            }
        }
        $result->close();
    } else {
        $response[KEY_ERROR_CODE] = ERROR_CODE_ERROR;
        $response[KEY_ERROR_MESSAGE] = 'เกิดข้อผิดพลาดในการบันทึกข้อมูล';
        $errMessage = $db->error;
        $response[KEY_ERROR_MESSAGE_MORE] = "$errMessage\nSQL: $sql";
    }

    $sql = "INSERT INTO `booking` (`user_id`, `parking_place_id`) "
        . " VALUES ($userId, $parkingPlaceId)";
    $result = $db->query($sql);
    if ($result) {
        $response[KEY_ERROR_CODE] = ERROR_CODE_SUCCESS;
        $response[KEY_ERROR_MESSAGE] = 'บันทึกข้อมูลสำเร็จ';
        $response[KEY_ERROR_MESSAGE_MORE] = '';
    } else {
        $response[KEY_ERROR_CODE] = ERROR_CODE_ERROR;
        $response[KEY_ERROR_MESSAGE] = 'เกิดข้อผิดพลาดในการบันทึกข้อมูล';
        $errMessage = $db->error;
        $response[KEY_ERROR_MESSAGE_MORE] = "$errMessage\nSQL: $sql";
    }
}

function doUpdateBooking()
{
    global $db, $response;

    $bookingId = $db->real_escape_string($_POST['booking_id']);
    $status = (int)($db->real_escape_string($_POST['status']));

    $setPayDate = ($status === 1 ? ', `pay_date`=NOW()' : '');
    $sql = "UPDATE `booking` SET `status`=$status $setPayDate WHERE id=$bookingId";
    $result = $db->query($sql);
    if ($result) {
        $response[KEY_ERROR_CODE] = ERROR_CODE_SUCCESS;
        $response[KEY_ERROR_MESSAGE] = 'บันทึกข้อมูลสำเร็จ';
        $response[KEY_ERROR_MESSAGE_MORE] = '';
    } else {
        $response[KEY_ERROR_CODE] = ERROR_CODE_ERROR;
        $response[KEY_ERROR_MESSAGE] = 'เกิดข้อผิดพลาดในการบันทึกข้อมูล: ' . $db->error;
        $errMessage = $db->error;
        $response[KEY_ERROR_MESSAGE_MORE] = "$errMessage\nSQL: $sql";
    }
}

function doGetSlip()
{
    global $db, $response;

    $providerId = $db->real_escape_string($_POST['provider_id']);
    $userId = $db->real_escape_string($_POST['user_id']);

    if (isset($providerId)) {
        $sql = "SELECT b.id, b.pay_date, u.first_name, u.last_name, pp.fee, pp.provider_id
                FROM booking b 
                    INNER JOIN parking_place pp 
                        ON b.parking_place_id = pp.id 
                    INNER JOIN user u 
                        ON b.user_id = u.id 
                WHERE pp.provider_id = $providerId AND b.status > 0
                ORDER BY b.id DESC";

    } else if (isset($userId)) {
        $sql = "SELECT b.id, b.pay_date, u.first_name, u.last_name, pp.fee, pp.provider_id
                FROM booking b 
                    INNER JOIN parking_place pp 
                        ON b.parking_place_id = pp.id 
                    INNER JOIN user u 
                        ON b.user_id = u.id 
                WHERE b.user_id = $userId AND b.status > 0
                ORDER BY b.id DESC";

    } else {
        $response[KEY_ERROR_CODE] = ERROR_CODE_ERROR;
        $response[KEY_ERROR_MESSAGE] = 'ระบุ parameter ไม่ครบ';
        $response[KEY_ERROR_MESSAGE_MORE] = '';
        return;
    }

    if ($result = $db->query($sql)) {
        $response[KEY_ERROR_CODE] = ERROR_CODE_SUCCESS;
        $response[KEY_ERROR_MESSAGE] = 'อ่านข้อมูลสำเร็จ';
        $response[KEY_ERROR_MESSAGE_MORE] = '';
        $response['data_list'] = array();

        while ($row = $result->fetch_assoc()) {
            $bookingSlip = array();
            $bookingSlip['booking_id'] = $row['id'];
            $bookingSlip['pay_date'] = $row['pay_date'];
            $bookingSlip['user_first_name'] = $row['first_name'];
            $bookingSlip['user_last_name'] = $row['last_name'];
            $bookingSlip['fee'] = (int)$row['fee'];
            array_push($response['data_list'], $bookingSlip);
        }
        $result->close();
    } else {
        $response[KEY_ERROR_CODE] = ERROR_CODE_ERROR;
        $response[KEY_ERROR_MESSAGE] = 'เกิดข้อผิดพลาดในการอ่านข้อมูล';
        $errMessage = $db->error;
        $response[KEY_ERROR_MESSAGE_MORE] = "$errMessage\nSQL: $sql";
    }
}

function doSaveRating($type)
{
    global $db, $response;

    $bookingId = $db->real_escape_string($_POST['booking_id']);
    $rating = $db->real_escape_string($_POST['rating']);

    $sql = '';
    if ($type === TYPE_USER) {
        $sql = "UPDATE `booking` SET `rating_user`=$rating WHERE id=$bookingId";
    } else if ($type == TYPE_PROVIDER) {
        $sql = "UPDATE `booking` SET `rating_provider`=$rating WHERE id=$bookingId";
    }
    if ($sql !== '') {
        $result = $db->query($sql);
        if ($result) {
            $response[KEY_ERROR_CODE] = ERROR_CODE_SUCCESS;
            $response[KEY_ERROR_MESSAGE] = 'บันทึกข้อมูลสำเร็จ';
            $response[KEY_ERROR_MESSAGE_MORE] = '';
        } else {
            $response[KEY_ERROR_CODE] = ERROR_CODE_ERROR;
            $response[KEY_ERROR_MESSAGE] = 'เกิดข้อผิดพลาดในการบันทึกข้อมูล: ' . $db->error;
            $errMessage = $db->error;
            $response[KEY_ERROR_MESSAGE_MORE] = "$errMessage\nSQL: $sql";
        }
    } else {
        $response[KEY_ERROR_CODE] = ERROR_CODE_ERROR;
        $response[KEY_ERROR_MESSAGE] = 'เกิดข้อผิดพลาดในการบันทึกข้อมูล: SQL command is empty';
        $response[KEY_ERROR_MESSAGE_MORE] = '';
    }
}

function doUpdateFirebaseToken($type)
{
    global $db, $response;

    $id = $db->real_escape_string($_POST['id']);
    $token = $db->real_escape_string($_POST['token']);

    $sql = '';
    if ($type === TYPE_USER) {
        $sql = "UPDATE `user` SET `firebase_token`='$token' WHERE id=$id";
    } else if ($type == TYPE_PROVIDER) {
        $sql = "UPDATE `provider` SET `firebase_token`='$token' WHERE id=$id";
    }
    if ($sql !== '') {
        $result = $db->query($sql);
        if ($result) {
            $response[KEY_ERROR_CODE] = ERROR_CODE_SUCCESS;
            $response[KEY_ERROR_MESSAGE] = 'บันทึก Firebase Token สำเร็จ';
            $response[KEY_ERROR_MESSAGE_MORE] = '';
        } else {
            $response[KEY_ERROR_CODE] = ERROR_CODE_ERROR;
            $response[KEY_ERROR_MESSAGE] = 'เกิดข้อผิดพลาดในการบันทึก Firebase Token: ' . $db->error;
            $errMessage = $db->error;
            $response[KEY_ERROR_MESSAGE_MORE] = "$errMessage\nSQL: $sql";
        }
    } else {
        $response[KEY_ERROR_CODE] = ERROR_CODE_ERROR;
        $response[KEY_ERROR_MESSAGE] = 'เกิดข้อผิดพลาดในการบันทึกข้อมูล: SQL command is empty';
        $response[KEY_ERROR_MESSAGE_MORE] = '';
    }
}

?>
