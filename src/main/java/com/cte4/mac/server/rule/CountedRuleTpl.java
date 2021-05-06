package com.cte4.mac.server.rule;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;

import com.cte4.mac.server.model.annotation.ElementDescriptor;
import com.cte4.mac.server.model.annotation.MethodDescriptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CountedRuleTpl extends RuleTplProcessor {

    private String annotationClass = "com.e4.maclient.annotation.Counted";
    private String ruleTmpFile = "COUNTED.ftl";
    private String ruleName = "Counted";
    @Value("${rule.func.scripts}")
    private String ruleScriptLoc;

    @Override
    public boolean isAccepted(ElementDescriptor elementDescriptor) {
        if (elementDescriptor instanceof MethodDescriptor && elementDescriptor.getAnnotations().stream()
                .filter(t -> t.getName().equals(annotationClass)).findAny().isPresent()) {
            return true;
        }
        return false;
    }

    @Override
    public String generateScript(ElementDescriptor elementDescriptor, ElementDescriptor pDescriptor) {
        log.info("> TODO: generate rule script **");
        StringWriter swOut = new StringWriter();
        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
            URL loc = ClassLoader.getSystemResource(ruleScriptLoc);
            File scriptLoc = new File(loc.getPath());
            cfg.setDirectoryForTemplateLoading(scriptLoc);
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

            Template tpl = cfg.getTemplate(this.ruleTmpFile);
            tpl.process(elementDescriptor, swOut);
        } catch (Exception e) {
            log.error("fail to load rule template", e);
        }
        return swOut.toString();
    }

    @Override
    public String getRuleName() {
        return this.ruleName;
    }

}
