<?php

declare(strict_types=1);

ini_set('display_errors', '0');

header('Content-Type: application/json; charset=utf-8');

function sendJson(array $data, int $statusCode = 200): never
{
    http_response_code($statusCode);

    echo json_encode(
        $data,
        JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES
    );

    exit;
}

$requestMethod = $_SERVER['REQUEST_METHOD'] ?? '';

if ($requestMethod !== 'POST') {
    header('Allow: POST');
    sendJson([
        'success' => false,
        'message' => 'Only POST requests are allowed.'
    ], 405);
}

// Parse request body (supports raw JSON input or standard POST form data)
$input = [];
$rawInput = file_get_contents('php://input');

if (!empty($rawInput)) {
    try {
        $input = json_decode($rawInput, true, 512, JSON_THROW_ON_ERROR);
    } catch (JsonException $e) {
        sendJson([
            'success' => false,
            'message' => 'Invalid JSON payload provided in request body.'
        ], 400);
    }
} else {
    $input = $_POST;
}

// Validate input fields (Customize 'name' and 'course' as needed)
$name = trim((string)($input['name'] ?? ''));
$course = trim((string)($input['course'] ?? ''));

if ($name === '' || $course === '') {
    sendJson([
        'success' => false,
        'message' => 'Missing required fields: name and course are required.'
    ], 400);
}

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

if (!isset($jsonData['students']) || !is_array($jsonData['students'])) {
    sendJson([
        'success' => false,
        'message' => 'The students array is missing from students.json.'
    ], 500);
}

// Determine the next auto-incrementing ID
$maxId = 0;
foreach ($jsonData['students'] as $s) {
    $currentId = (int)($s['id'] ?? 0);
    if ($currentId > $maxId) {
        $maxId = $currentId;
    }
}
$newId = $maxId + 1;

// Construct new student record
$newStudent = [
    'id' => $newId,
    'name' => $name,
    'course' => $course
];

// Append new student to the array
$jsonData['students'][] = $newStudent;

// Save updated JSON back to file with atomic lock
$encodedData = json_encode($jsonData, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);

if (file_put_contents($jsonFile, $encodedData, LOCK_EX) === false) {
    sendJson([
        'success' => false,
        'message' => 'Failed to write updated data to students.json.'
    ], 500);
}

sendJson([
    'success' => true,
    'message' => 'Student added successfully.',
    'data' => $newStudent
], 201);