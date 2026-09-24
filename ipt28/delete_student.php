<?php

declare(strict_types=1);

ini_set('display_errors', '0');

header('Content-Type: application/json; charset=utf-8');

/**
 * Send a JSON response and stop the script.
 */
function sendJson(array $data, int $statusCode = 200): never
{
    http_response_code($statusCode);

    echo json_encode(
        $data,
        JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES
    );

    exit;
}

/*
|--------------------------------------------------------------------------
| 1. Allow DELETE requests only
|--------------------------------------------------------------------------
*/

$requestMethod = $_SERVER['REQUEST_METHOD'] ?? '';

if ($requestMethod !== 'DELETE') {
    header('Allow: DELETE');

    sendJson([
        'success' => false,
        'message' => 'Only DELETE requests are allowed.'
    ], 405);
}

/*
|--------------------------------------------------------------------------
| 2. Extract ID to delete
|--------------------------------------------------------------------------
*/

$targetId = $_GET['id'] ?? null;

if ($targetId === null) {
    $inputContent = file_get_contents('php://input');
    if (!empty($inputContent)) {
        $inputData = json_decode($inputContent, true);
        $targetId = $inputData['id'] ?? null;
    }
}

if ($targetId === null || !is_numeric($targetId)) {
    sendJson([
        'success' => false,
        'message' => 'Student ID is required via URL parameter (?id=X) or JSON body.'
    ], 400);
}

$targetId = (int)$targetId;

/*
|--------------------------------------------------------------------------
| 3. Load students.json
|--------------------------------------------------------------------------
*/

$jsonFile = __DIR__ . '/students.json';

if (!file_exists($jsonFile)) {
    sendJson([
        'success' => false,
        'message' => 'students.json was not found.'
    ], 500);
}

$jsonContent = file_get_contents($jsonFile);

if ($jsonContent === false) {
    sendJson([
        'success' => false,
        'message' => 'Unable to read students.json.'
    ], 500);
}

try {
    $jsonData = json_decode(
        $jsonContent,
        true,
        512,
        JSON_THROW_ON_ERROR
    );
} catch (JsonException $exception) {
    sendJson([
        'success' => false,
        'message' => 'students.json contains invalid JSON.'
    ], 500);
}

$students = $jsonData['students'] ?? null;

if (!is_array($students)) {
    sendJson([
        'success' => false,
        'message' => 'The students array is missing from students.json.'
    ], 500);
}

/*
|--------------------------------------------------------------------------
| 4. Find and remove student
|--------------------------------------------------------------------------
*/

$foundIndex = null;
$deletedStudent = null;

foreach ($students as $index => $student) {
    if ((int)($student['id'] ?? 0) === $targetId) {
        $foundIndex = $index;
        $deletedStudent = $student;
        break;
    }
}

if ($foundIndex === null) {
    sendJson([
        'success' => false,
        'message' => "Cannot delete. Student with ID {$targetId} not found."
    ], 404);
}

// Remove student and re-index array
array_splice($students, $foundIndex, 1);
$jsonData['students'] = $students;

/*
|--------------------------------------------------------------------------
| 5. Save back to file
|--------------------------------------------------------------------------
*/

$updatedJson = json_encode(
    $jsonData,
    JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES
);

if (file_put_contents($jsonFile, $updatedJson) === false) {
    sendJson([
        'success' => false,
        'message' => 'Unable to write to students.json.'
    ], 500);
}

sendJson([
    'success' => true,
    'message' => "Student with ID {$targetId} deleted successfully.",
    'data'    => $deletedStudent
], 200);