package com.beeva.trustedoverlord.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProfileSupportCases {

    private List<Case> openCases;
    private List<Case> resolvedCases;


    public ProfileSupportCases() {
        this.openCases = Collections.synchronizedList(new ArrayList<>());
        this.resolvedCases= Collections.synchronizedList(new ArrayList<>());
    }

    public List<Case> getOpenCases() {
        return openCases;
    }

    public List<Case> getResolvedCases() {
        return resolvedCases;
    }

    public List<Case> addOpenCase(String id, String language, String status, String displayId, String subject){
        this.openCases.add(
                new Case()
                    .withId(id)
                    .withCreated(language)
                    .withStatus(status)
                    .withSubmittedBy(displayId)
                    .withSubject(subject));
        return this.openCases;
    }

    public List<Case> addResolvedCase(String id, String created, String status, String submittedBy, String subject){
        this.resolvedCases.add(
                new Case()
                        .withId(id)
                        .withCreated(created)
                        .withStatus(status)
                        .withSubmittedBy(submittedBy)
                        .withSubject(subject));
        return this.resolvedCases;
    }

    public class Case{
        private String id;
        private String created;
        private String status;
        private String submittedBy;
        private String subject;


        public String getId() {
            return id;
        }

        public Case withId(String id) {
            this.id = id;
            return this;
        }

        public String getCreated() {
            return created;
        }

        public Case withCreated(String created) {
            this.created = created;
            return this;
        }

        public String getStatus() {
            return status;
        }

        public Case withStatus(String status) {
            this.status = status;
            return this;
        }

        public String getSubmittedBy() {
            return submittedBy;
        }

        public Case withSubmittedBy(String submittedBy) {
            this.submittedBy = submittedBy;
            return this;
        }

        public String getSubject() {
            return subject;
        }

        public Case withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        @Override
        public String toString() {
            return "Case{" +
                    "id='" + id + '\'' +
                    ", created='" + created + '\'' +
                    ", status='" + status + '\'' +
                    ", submittedBy='" + submittedBy + '\'' +
                    ", subject='" + subject + '\'' +
                    '}';
        }
    }

    public <T> T to(ExportTo<T> func){
        return func.export(getOpenCases(), getResolvedCases());
    }

    @FunctionalInterface
    interface ExportTo<T> {
        T export(List<Case> openCases, List<Case> resolvedCases);
    }

}
