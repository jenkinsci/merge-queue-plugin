package com.coravy.hudson.plugins.github;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import java.util.Collection;
import java.util.Collections;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Stores the github related project properties.
 * <p>
 * As of now this is only the URL to the github project.
 * 
 * @author Monty Taylor <mordred@inaugust.com>
 */
public final class MergeQueueProjectProperty extends
        JobProperty<AbstractProject<?, ?>> {

    /**
     * This will the URL to the project main branch.
     */
    private String projectUrl;

    @DataBoundConstructor
    public MergeQueueProjectProperty(String projectUrl) {
        this.projectUrl = projectUrl;
    }

    /**
     * @return the projectUrl
     */
    public String getProjectUrl() {
        return projectUrl;
    }

    @Extension
    public static final class DescriptorImpl extends JobPropertyDescriptor {

        public DescriptorImpl() {
            super(MergeQueueProjectProperty.class);
            load();
        }

        public boolean isApplicable(Class<? extends Job> jobType) {
            return AbstractProject.class.isAssignableFrom(jobType);
        }

        public String getDisplayName() {
            return "MergeQueue project page";
        }

        @Override
        public JobProperty<?> newInstance(StaplerRequest req,
                JSONObject formData) throws FormException {
            MergeQueueProjectProperty tpp = req.bindJSON(
                    MergeQueueProjectProperty.class, formData);
            if (tpp.projectUrl == null) {
                tpp = null; // not configured
            }
            return tpp;
        }

    }
}
