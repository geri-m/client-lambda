package at.madlmayr;

public enum ToolEnum {

    SLACK("slack", "SlackCall"),
    JIRA("jira", "JiraCall"),
    ARTIFACTORY("artifactory", "ArtifactoryCall");

    private final String name;
    private final String functionName;

    ToolEnum(final String name, final String functionName){
        this.name = name;
        this.functionName = functionName;
    }

    public String getName() {
        return name;
    }

    public String getFunctionName() {
        return functionName;
    }


}
