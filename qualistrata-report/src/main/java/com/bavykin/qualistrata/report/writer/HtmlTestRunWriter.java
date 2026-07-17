package com.bavykin.qualistrata.report.writer;

import com.bavykin.qualistrata.report.model.TestRun;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Writes a self-contained interactive HTML visualization of a test run.
 *
 * <p>The generated document embeds the canonical JSON representation produced
 * by {@link JsonTestRunWriter}. It requires no web server, external scripts,
 * stylesheets, fonts, or network access.</p>
 */
public final class HtmlTestRunWriter implements TestRunWriter {

    private final JsonTestRunWriter jsonWriter;

    /**
     * Creates an HTML writer backed by a default canonical JSON writer.
     */
    public HtmlTestRunWriter() {
        this(new JsonTestRunWriter());
    }

    /**
     * Creates an HTML writer backed by the supplied canonical JSON writer.
     *
     * @param jsonWriter writer used to serialize the embedded canonical model
     */
    public HtmlTestRunWriter(JsonTestRunWriter jsonWriter) {
        this.jsonWriter = Objects.requireNonNull(jsonWriter, "jsonWriter");
    }

    /**
     * Returns the conventional HTML path next to a JSON report.
     *
     * <p>For example, {@code test-run.json} becomes
     * {@code test-run.html}.</p>
     *
     * @param jsonOutputPath canonical JSON report path
     * @return normalized sibling HTML path
     */
    public static Path siblingPath(Path jsonOutputPath) {
        Objects.requireNonNull(jsonOutputPath, "jsonOutputPath");
        Path normalized = jsonOutputPath.normalize();
        Path fileName = normalized.getFileName();
        if (fileName == null || fileName.toString().isBlank()) {
            throw new IllegalArgumentException(
                    "JSON output path must have a file name"
            );
        }

        String name = fileName.toString();
        String htmlName = name.endsWith(".json")
                ? name.substring(0, name.length() - 5) + ".html"
                : name + ".html";
        Path parent = normalized.getParent();
        return parent == null ? Path.of(htmlName) : parent.resolve(htmlName);
    }

    /**
     * Builds a complete standalone HTML document for the supplied test run.
     *
     * @param testRun immutable test-run model
     * @return complete UTF-8 HTML document
     */
    public String toHtml(TestRun testRun) {
        Objects.requireNonNull(testRun, "testRun");
        String embeddedJson = jsonWriter.toJson(testRun)
                .replace("</", "<\\/");

        return TEMPLATE.replace("__REPORT_JSON__", embeddedJson);
    }

    /**
     * Writes the standalone HTML report, creating parent directories as needed.
     *
     * @param testRun immutable test-run model
     * @param outputPath destination HTML path
     * @throws ReportWriteException when the report cannot be written
     */
    @Override
    public void write(TestRun testRun, Path outputPath) {
        Objects.requireNonNull(testRun, "testRun");
        Objects.requireNonNull(outputPath, "outputPath");

        Path absoluteOutput = outputPath.toAbsolutePath().normalize();
        Path parent = absoluteOutput.getParent();
        if (parent == null) {
            throw new IllegalArgumentException(
                    "Output path must have a parent directory"
            );
        }

        try {
            Files.createDirectories(parent);
            Files.writeString(
                    absoluteOutput,
                    toHtml(testRun),
                    StandardCharsets.UTF_8
            );
        } catch (IOException exception) {
            throw new ReportWriteException(
                    "Unable to write HTML test-run report to "
                            + absoluteOutput,
                    exception
            );
        }
    }

    private static final String TEMPLATE = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>QualiStrata Test Run</title>
<style>
:root{--bg:#0b1020;--p:#131a2c;--p2:#182239;--t:#edf2ff;--m:#9ca9c8;--b:#263453;--a:#7ea2ff;--ok:#4fd18b;--bad:#ff6b7a;--skip:#f7c65d;--abort:#b48cff}
*{box-sizing:border-box}body{margin:0;font-family:Inter,Segoe UI,Arial,sans-serif;background:radial-gradient(circle at top right,rgba(126,162,255,.15),transparent 32%),var(--bg);color:var(--t)}
main{max-width:1500px;margin:auto;padding:28px}.head{display:flex;justify-content:space-between;gap:20px;margin-bottom:22px}h1{margin:0 0 8px}.muted{color:var(--m)}.code{font-family:Consolas,monospace;font-size:12px}
.badge,.pill{display:inline-block;border-radius:999px;padding:6px 10px}.badge{border:1px solid var(--b);background:var(--p)}
.grid{display:grid;grid-template-columns:repeat(6,1fr);gap:14px;margin-bottom:18px}.card{background:linear-gradient(180deg,var(--p2),var(--p));border:1px solid var(--b);border-radius:16px;padding:18px}
.label{color:var(--m);font-size:12px;text-transform:uppercase;letter-spacing:.08em}.value{font-size:27px;font-weight:700;margin-top:8px}.two{display:grid;grid-template-columns:1fr 1fr;gap:18px;margin-bottom:18px}
.barrow{display:flex;align-items:center;gap:10px;margin:12px 0}.barname{width:72px;color:var(--m)}.bar{height:12px;flex:1;background:#0e1527;border-radius:999px;overflow:hidden}.fill{height:100%;border-radius:999px}.count{width:48px;text-align:right}
.controls{display:flex;gap:10px;flex-wrap:wrap;margin-bottom:14px}input,select{background:#0f1628;color:var(--t);border:1px solid var(--b);border-radius:10px;padding:10px 12px}input{flex:1;min-width:260px}
.wrap{overflow:auto;border:1px solid var(--b);border-radius:14px}table{width:100%;border-collapse:collapse;min-width:1000px}th,td{padding:11px 13px;border-bottom:1px solid var(--b);text-align:left}th{position:sticky;top:0;background:#121a2d;color:var(--m);font-size:12px;text-transform:uppercase}.PASSED{color:var(--ok);background:rgba(79,209,139,.13)}.FAILED{color:var(--bad);background:rgba(255,107,122,.13)}.SKIPPED{color:var(--skip);background:rgba(247,198,93,.13)}.ABORTED{color:var(--abort);background:rgba(180,140,255,.13)}
.slow{display:grid;grid-template-columns:1fr auto;gap:10px;padding:9px 11px;margin:8px 0;background:#0f1628;border:1px solid var(--b);border-radius:10px}
@media(max-width:1000px){.grid{grid-template-columns:repeat(3,1fr)}.two{grid-template-columns:1fr}}@media(max-width:620px){main{padding:15px}.grid{grid-template-columns:repeat(2,1fr)}.head{display:block}}
</style>
</head>
<body>
<main>
<div class="head"><div><h1>QualiStrata Test Run</h1><div class="muted">Source: <b id="source"></b><br>Run ID: <span class="code" id="runId"></span></div></div><div><span class="badge" id="complete"></span></div></div>
<div class="grid" id="metrics"></div>
<div class="two"><section class="card"><h3>Status distribution</h3><div id="bars"></div></section><section class="card"><h3>Run metadata</h3><div id="metadata"></div></section></div>
<div class="two"><section class="card"><h3>Slowest tests</h3><div id="slowest"></div></section><section class="card"><h3>Coverage readiness</h3><div id="coverage"></div></section></div>
<section class="card"><h3>Tests</h3><div class="controls"><input id="q" placeholder="Search class, method, feature, component or tag"><select id="status"><option value="">All statuses</option><option>PASSED</option><option>FAILED</option><option>SKIPPED</option><option>ABORTED</option></select></div><div class="wrap"><table><thead><tr><th>Status</th><th>Class</th><th>Method</th><th>Display name</th><th>Duration</th><th>Feature</th><th>Component</th><th>Tags</th></tr></thead><tbody id="rows"></tbody></table></div><p class="muted" id="shown"></p></section>
</main>
<script>
const report=__REPORT_JSON__;
const esc=v=>String(v??"").replace(/[&<>"']/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]));
const tests=report.tests.map(x=>{const t=x.test,i=t.identifier,m=t.metadata,a=x.attempts.at(-1)?.result||{};return{status:x.finalStatus,className:i.className||"",methodName:i.methodName||"",displayName:t.displayName,duration:x.totalDurationMs,feature:m.feature,component:m.component,description:m.description,tags:t.tags||[],cases:m.coveredTestCases||[],failure:a.failure}});
const s=report.summary,total=s.total||1,avg=tests.length?tests.reduce((n,t)=>n+t.duration,0)/tests.length:0;
source.textContent=report.source;runId.textContent=report.runId;complete.textContent=report.complete?"Complete":"Incomplete";
metrics.innerHTML=[["Total",s.total],["Passed",s.passed],["Failed",s.failed],["Skipped",s.skipped],["Run duration",report.durationMs+" ms"],["Average test",avg.toFixed(2)+" ms"]].map(x=>`<div class="card"><div class="label">${x[0]}</div><div class="value">${x[1]}</div></div>`).join("");
bars.innerHTML=[["Passed",s.passed,"var(--ok)"],["Failed",s.failed,"var(--bad)"],["Skipped",s.skipped,"var(--skip)"],["Aborted",s.aborted,"var(--abort)"]].map(x=>`<div class="barrow"><div class="barname">${x[0]}</div><div class="bar"><div class="fill" style="width:${x[1]/total*100}%;background:${x[2]}"></div></div><div class="count">${x[1]}</div></div>`).join("");
metadata.innerHTML=`<p><span class="muted">Schema:</span> ${esc(report.schemaVersion)}</p><p><span class="muted">Started:</span> <span class="code">${esc(report.startedAt)}</span></p><p><span class="muted">Finished:</span> <span class="code">${esc(report.finishedAt)}</span></p><p><span class="muted">Metadata:</span> <span class="code">${esc(JSON.stringify(report.metadata))}</span></p>`;
slowest.innerHTML=[...tests].sort((a,b)=>b.duration-a.duration).slice(0,10).map(t=>`<div class="slow"><div>${esc(t.displayName)}<div class="muted code">${esc(t.className)}</div></div><b>${t.duration} ms</b></div>`).join("");
coverage.innerHTML=[["With feature",tests.filter(t=>t.feature).length],["With component",tests.filter(t=>t.component).length],["With description",tests.filter(t=>t.description).length],["Linked to test cases",tests.filter(t=>t.cases.length).length],["With tags",tests.filter(t=>t.tags.length).length],["Zero duration",tests.filter(t=>t.duration===0).length]].map(x=>`<p><span class="muted">${x[0]}:</span> <b>${x[1]}</b></p>`).join("");
function render(){const query=q.value.toLowerCase(),st=status.value;const f=tests.filter(t=>(!st||t.status===st)&&[t.className,t.methodName,t.displayName,t.feature,t.component,...t.tags].join(" ").toLowerCase().includes(query));rows.innerHTML=f.map(t=>`<tr><td><span class="pill ${esc(t.status)}">${esc(t.status)}</span></td><td class="code">${esc(t.className)}</td><td class="code">${esc(t.methodName)}</td><td>${esc(t.displayName)}</td><td>${t.duration} ms</td><td>${esc(t.feature||"—")}</td><td>${esc(t.component||"—")}</td><td>${esc(t.tags.join(", ")||"—")}</td></tr>`).join("");shown.textContent=`Showing ${f.length} of ${tests.length} tests`}
q.oninput=render;status.onchange=render;render();
</script>
</body>
</html>
""";
}
