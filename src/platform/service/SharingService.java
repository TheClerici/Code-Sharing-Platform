package platform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import platform.exceptions.CodeNotFound;
import platform.models.CodeModel;
import platform.repository.SharingRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SharingService {

    @Autowired
    private final SharingRepository sharingRepository;

    public SharingService(SharingRepository sharingRepository) {
        this.sharingRepository = sharingRepository;
    }

    private void updateTimeAndViews(CodeModel codeModel) {
        if (codeModel.isRestrictedView()) {
            if (codeModel.getViews() == 0) {
                sharingRepository.deleteById(codeModel.getId());
                throw new CodeNotFound();
            }

            codeModel.setViews(codeModel.getViews() - 1);
            sharingRepository.save(codeModel);
        }

        if (codeModel.isRestrictedTime()) {
            long dif = LocalDateTime.now()
                    .until(codeModel.getLocalDateTime().plusSeconds(codeModel.getTime()), ChronoUnit.SECONDS);
            if (dif <= 0) {
                sharingRepository.deleteById(codeModel.getId());
                throw new CodeNotFound();
            }

            codeModel.setTime(dif);
        }
    }

    public ResponseEntity<?> getAPICodeByID(String id) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");

        CodeModel codeModel = sharingRepository.findById(id).orElseThrow(CodeNotFound::new);

        if (codeModel.isRestrictedTime() || codeModel.isRestrictedView()) {
            updateTimeAndViews(codeModel);
        }

        return ResponseEntity.ok().headers(responseHeaders).body(codeModel);
    }

    public ResponseEntity<?> getHTMLCodeByID(String id) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "text/html");

        CodeModel codeModel = sharingRepository.findById(id).orElseThrow(CodeNotFound::new);

        if (codeModel.isRestrictedTime() || codeModel.isRestrictedView()) {
            updateTimeAndViews(codeModel);
        }

        if (codeModel.isRestrictedTime() && codeModel.isRestrictedView()) {
            return ResponseEntity.ok().headers(responseHeaders).body(
                    "<html>\n" + "<head>\n" + "\t<title>Code</title>\n" + "</head>\n" + "<body>\n" +
                            "\t<pre id=\"code_snippet\"><code>\n" + codeModel.getCode() + "</code></pre>\n" +
                            "\t<span id=\"load_date\">\n" + codeModel.getDate() + "</span>\n" +
                            "\t<span id=\"time_restriction\">\n" + codeModel.getTime() + "</span>\n" +
                            "\t<span id=\"views_restriction\">\n" + codeModel.getViews() + "</span>\n" +"<script>\n" +
                            "\thljs.initHighlightingOnLoad();\n" + "</script>\n" + "</body>\n" + "</html>" );
        } else if (codeModel.isRestrictedTime()) {
            return ResponseEntity.ok().headers(responseHeaders).body(
                    "<html>\n" + "<head>\n" + "\t<title>Code</title>\n" + "</head>\n" + "<body>\n" +
                            "\t<pre id=\"code_snippet\"><code>\n" + codeModel.getCode() + "</code></pre>\n" +
                            "\t<span id=\"load_date\">\n" + codeModel.getDate() + "</span>\n" +
                            "\t<span id=\"time_restriction\">\n" + codeModel.getTime() + "</span>\n" +
                            "\thljs.initHighlightingOnLoad();\n" + "</script>\n" + "</body>\n" + "</html>" );
        } else if (codeModel.isRestrictedView()) {
            return ResponseEntity.ok().headers(responseHeaders).body(
                    "<html>\n" + "<head>\n" + "\t<title>Code</title>\n" + "</head>\n" + "<body>\n" +
                            "\t<pre id=\"code_snippet\"><code>\n" + codeModel.getCode() + "</code></pre>\n" +
                            "\t<span id=\"load_date\">\n" + codeModel.getDate() + "</span>\n" +
                            "\t<span id=\"views_restriction\">\n" + codeModel.getViews() + "</span>\n" +"<script>\n" +
                            "\thljs.initHighlightingOnLoad();\n" + "</script>\n" + "</body>\n" + "</html>" );
        }
        return ResponseEntity.ok().headers(responseHeaders).body(
                "<html>\n" + "<head>\n" + "\t<title>Code</title>\n" + "</head>\n" + "<body>\n" +
                        "\t<pre id=\"code_snippet\"><code>\n" + codeModel.getCode() + "</code></pre>\n" +
                        "\t<span id=\"load_date\">\n" + codeModel.getDate() + "</span>\n" +
                        "\thljs.initHighlightingOnLoad();\n" + "</script>\n" + "</body>\n" + "</html>" );
    }

    public ResponseEntity<?> postAPICode(Map<String, String> code) {
        CodeModel codeModel = new CodeModel();
        codeModel.setId(UUID.randomUUID().toString());
        codeModel.setLocalDateTime(LocalDateTime.now());
        codeModel.setDate(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS").format(codeModel.getLocalDateTime()));
        codeModel.setCode(code.get("code"));
        codeModel.setTime(Long.parseLong(code.get("time")));
        codeModel.setViews(Long.parseLong(code.get("views")));
        codeModel.setRestrictedTime(Long.parseLong(code.get("time")) != 0);
        codeModel.setRestrictedView(Long.parseLong(code.get("views")) != 0);
        sharingRepository.save(codeModel);
        return ResponseEntity.ok(Map.of("id", codeModel.getId()));
    }

    public ResponseEntity<?> getNewCode() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "text/html");
        return ResponseEntity.ok().headers(responseHeaders).body(
                """
                <html>
                <head>
                \t<title>Create</title>
                </head>
                <body>
                \t<textarea id="code_snippet">// Write your code here ...</textarea>
                \t<input id="time_restriction" type="text"/>
                \t<input id="views_restriction" type="text"/>
                \t<button id="send_snippet" type="submit" onClick="send()">Submit</button>
                </body>
                </html>
                """
        );
    }

    public ResponseEntity<?> getAPILatestCodes() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        return ResponseEntity.ok().headers(responseHeaders).body(filter());
    }

    public ResponseEntity<?> getHTMLLatestCodes() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "text/html");
        List<CodeModel> list = filter();

        StringBuilder fullHTMLResponse = new StringBuilder();
        fullHTMLResponse.append("""
                <html>
                <head>
                \t<title>Latest</title>
                </head>
                <body>
                """);
        for (CodeModel model : list) {
            fullHTMLResponse.append("<span>\n").append(model.getDate()).append("\n").append("</span>\n");
            fullHTMLResponse.append("<pre><code>\n").append(model.getCode()).append("</code></pre>\n");
        }
        fullHTMLResponse.append("</body>\n").append("</html>");
        return ResponseEntity.ok().headers(responseHeaders).body(fullHTMLResponse);
    }

    public List<CodeModel> filter() {
        List<CodeModel> list = sharingRepository.findAllByRestrictedTimeAndRestrictedView(false, false);
        List<CodeModel> sortedList = list.stream().sorted(Comparator.comparing(CodeModel::getDate).reversed()).limit(10).toList();
        return sortedList.stream().sorted(Comparator.comparing(CodeModel::getDate).reversed()).toList();
    }
}