package hudson.plugins;

import hudson.Extension;
import hudson.Util;
import hudson.FilePath;
import static hudson.Util.*;
import hudson.model.BuildableItem;
import hudson.model.Item;
import hudson.scheduler.CronTabList;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.apache.commons.io.FileUtils;

import antlr.ANTLRException;

/** Triggers a build when there is a new approved pull request */
public class MergeQueueTrigger extends Trigger<BuildableItem> {

    URL url;

    public MergeQueueTrigger(String url) throws MalformedURLException {
        this(new URL(url));
    }
    
    public MergeQueueTrigger(URL url) {
        this.url = url;
    }
    
    @Override
    public void start(BuildableItem project, boolean newInstance) {
    	super.start(project, newInstance);
        try {
            this.tabs = CronTabList.create("* * * * *");
        } catch (ANTLRException e) {
            throw new RuntimeException("Bug! couldn't schedule poll");
        }   	
    }

    private static final Logger LOGGER =
        Logger.getLogger(MergeQueueTrigger.class.getName());

    private File getFingerprintFile() {
	return new File(job.getRootDir(), "url-change-trigger-oldmd5");
    }

    @Override
    public void run() {
        try {
            LOGGER.log(Level.FINER, "Testing the file {0}", url);
            String currentMd5 = Util.getDigestOf(url.openStream());

            String oldMd5;
            File file = getFingerprintFile();
            if (!file.exists()) {
                oldMd5 = "null";
            } else {
                oldMd5 = new FilePath(file).readToString().trim();
            }
            if (!currentMd5.equalsIgnoreCase(oldMd5)) {
                LOGGER.log(Level.FINE,
                        "Differences found in the file {0}. >{1}< != >{2}<",
                        new Object[]{
                                url, oldMd5, currentMd5,
                        });

                FileUtils.writeStringToFile(file, currentMd5);
                job.scheduleBuild(new UrlChangeCause(url));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public URL getUrl() {
        return url;
    }

    @Extension
    public static final class DescriptorImpl extends TriggerDescriptor {

        public DescriptorImpl() {
            super(MergeQueueTrigger.class);
        }

        @Override
        public boolean isApplicable(Item item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Build when a URL's content changes";
        }
        
        @Override
        public String getHelpFile() {
            return "/plugin/url-change-trigger/help-whatIsMergeQueueTrigger.html";
        }
        
        /**
         * Performs syntax check.
         */
        public FormValidation doCheck(@QueryParameter("mergeQueueTrigger.url") String url) {
            try {
                new URL(fixNull(url));
                return FormValidation.ok();
            } catch (MalformedURLException e) {
                return FormValidation.error(e.getMessage());
            }
        }
        
        @Override
        public MergeQueueTrigger newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            String url = formData.getString("url");
            try {
                return new MergeQueueTrigger(url);
            } catch (MalformedURLException e) {
                throw new FormException("Invalid URL: " + url, e, "");
            }
        }
        
    }
}
