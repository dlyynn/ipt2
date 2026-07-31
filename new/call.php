<?php
    $command = "java Sum 10 5";
    exec($command, $output, $return);

    if($return === 0)
    {
        echo "Java program executed successfully:\n";
        foreach($output as $line)
        {
            echo $line . "\n";
        }
    }
    else
    {
        echo "Error running Java Program!.\n";
    }
?>