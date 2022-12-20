package platform.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import platform.service.SharingService;

import java.util.Map;

@RestController
public class SharingController {

    @Autowired
    private final SharingService sharingService;

    public SharingController(SharingService sharingService) {
        this.sharingService = sharingService;
    }

    @GetMapping("/api/code/{id}")
    public ResponseEntity<?> getAPICodeByID(@PathVariable String id) {
        return sharingService.getAPICodeByID(id);
    }

    @GetMapping("/code/{id}")
    public ResponseEntity<?> getHTMLCodeByID(@PathVariable String id) {
        return sharingService.getHTMLCodeByID(id);
    }

    @PostMapping("/api/code/new")
    public ResponseEntity<?> postAPICode(@RequestBody Map<String, String> code) {
        return sharingService.postAPICode(code);
    }

    @GetMapping("/code/new")
    public ResponseEntity<?> getNewCode() { return sharingService.getNewCode(); }

    @GetMapping("/api/code/latest")
    public ResponseEntity<?> getAPILatestCodes() { return sharingService.getAPILatestCodes(); }

    @GetMapping("/code/latest")
    public ResponseEntity<?> getHTMLLatestCodes() {
        return sharingService.getHTMLLatestCodes(); }
}
