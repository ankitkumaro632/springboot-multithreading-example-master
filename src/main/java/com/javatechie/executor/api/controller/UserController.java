package com.javatechie.executor.api.controller;

import com.javatechie.executor.api.config.AsyncConfig;
import com.javatechie.executor.api.config.ScheduledJob;
import com.javatechie.executor.api.entity.User;
import com.javatechie.executor.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
public class UserController {
    @Autowired
    private UserService service;

    private final ScheduledJob scheduledJob;

    public UserController(UserService service, ScheduledJob scheduledJob) {
        this.service = service;
        this.scheduledJob = scheduledJob;
    }

    // constructor

    @GetMapping("/launch")
    String toggle() {
        scheduledJob.toggle();
        return "toggle run";
    }

    @PostMapping(value = "/users", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
    public ResponseEntity<Object> saveUsers(@RequestParam(value = "files") MultipartFile[] files) throws Exception {
        if (files != null) {
            for (MultipartFile file : files) {
                service.saveUsers(file);
            }
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping(value = "/users", produces = "application/json")
    public CompletableFuture<ResponseEntity> findAllUsers() {
       return  service.findAllUsers().thenApply(ResponseEntity::ok);
    }


    @GetMapping(value = "/getUsersByThread", produces = "application/json")
    public  ResponseEntity getUsers(){
        CompletableFuture<List<User>> users1=service.findAllUsers();
        CompletableFuture<List<User>> users2=service.findAllUsers();
        CompletableFuture<List<User>> users3=service.findAllUsers();
        CompletableFuture.allOf(users1,users2,users3).join();
        return  ResponseEntity.status(HttpStatus.OK).build();
    }



    @GetMapping("/runThread")
    public ResponseEntity<String> runThread() {
        AsyncConfig thread = new AsyncConfig();
        new Thread(thread).start();

        // Wait for the thread to finish
        while (thread.getResult() == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Handle exception
            }
        }

        // Return response with result from thread
        return ResponseEntity.ok(thread.getResult());
    }

    @GetMapping("/runProcess")
    public String runProcess() {
        try {
            ProcessBuilder pb = new ProcessBuilder("my-command", "my-arg1", "my-arg2");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // start a separate thread to read the output of the process
            Thread thread = new Thread(() -> {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            thread.start();

            return "Process started.";
        } catch (IOException e) {
            e.printStackTrace();
            return "Error starting process: " + e.getMessage();
        }
    }
}
