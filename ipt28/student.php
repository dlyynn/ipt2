<?php

declare(strict_types=1);

ini_set('display_errors', '0');

header('Content-Type: application/json; charset=utf-8');

*Get Method

function sendJson(array $data, int $statusCode = 200): never
{
    http response code($statusCode);

    echo json_encode(
        $data,
        JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES
    );

    exit;
}

function getStudentById(array $students, int $studentId): ?array
{
    foreach ($students as $student)
    {
        if((int) ($student['id']?? 0) === $studentId)
        {
            return $student;
        }
    }

    return null;
}

$requestMethod = $_SERVER['REQUEST_METHOD'] ?? '';

if($requestMethod !== 'GET')
{
    header('Allow: GET');

    sendJson
    (
        ['success' => false,
        'message' => 'Only GET requests are allowed.'], 405);
}

$jsonFile = __DIR__ . '/students.json';

if (!file_exists($jsonFile))
{
    sendJson([
        'success' => false,
        'message' => 'students.json was not found.'
    ], 500);
}

$jsonContent = file_get_contents($jsonFile);

if($jsonContent === false)
{
    sendJson([
        'success' => false,
        'message' => 'Unable to read students.json.'
    ], 500;)
}

try
{
    $jsonData = json_decode(
        $jsonContent,
        true,
        512,
        JSON_THROW_ON_ERROR
    );
} catch (JsonException $exception)
{
    sendJson([
        'success' => false,
        'message' => 'students.json contains imvalid JSON.'
    ], 500);
}

$students = $jsonData['students'] ?? null;

if (!is_array($students))
{
    sendJson([
        'success' => false,
        'message' => 'the students array is missing from students.json.'
    ], 500)
}

if (isset($_GET['id']))
{
    $studentId = filter_var
    (
        $_GET['id'],
        FILTER_VALIDATE_INT,
        [
            'options' => [
                'min_range' => 1
            ]
        ]
    );

    if ($studentId === false)
    {
        sendJson([
            'success' => false,
            'message' => 'the student ID must be a positive integer.'
        ], 400);
    }
}

    $student = getStudentById($students, $studentId);

        if ($student === null)
        {
            sendJson([
            'success' => false,
            'message' => 'student not found.'
        ], 404);
        
        sendJson([
            'success' => true,
            'data' => $student
        ]);
}

usort(
    $students,
    fn(array $first, array $second): int =>
        (int) ($first['id'] ?? 0) <=> (int) ($second['id'] ?? 0)
);

sendJson([
    'succes' => true,
    'count' => count($students),
    'data' => $students
]);
