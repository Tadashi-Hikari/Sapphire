package net.carrolltech.athena.essence.Body;

import org.json.JSONObject;

import java.io.File;
import java.util.Map;

// All data should be stored persistently, to withstand the OS killing Sapphire

public class Memory{

    // All known available routes, with String names. The state is held in CoreState.activeIds
    /*
    The name for a pipeline might be a PackageName;ClassName String, an Action String, a default
    feature (such as SPEAK, DEFAULT, etc), or even an Alias. This is done to simplify the process
    of requesting a pipeline
     */
    public static Map<String, Pipeline> pipelines = null;
    public static Map<String, JSONObject> moduleInfo = null;

    public Memory(){
        loadPipelines();
        // I don't know if I want to do this, but it might be needed for knowing what runs in the background, and what in the foreground
        //loadAvailableModules();
    }

    private void loadPipelines(){
        // Just an example of how I will use this
        File pipelineFile = new File(Utilities.PIPELINE_TABLE);
        for(String pipelineString: pipelineFile.list())
            pipelines.put("default",new Pipeline(pipelineString));
    }
}
