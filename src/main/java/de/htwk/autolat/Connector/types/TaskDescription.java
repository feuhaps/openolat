/*
 * Warning: This is a generated file. Edit at your own risk.
 * generated by Gen.hs on Thu Jan 21 17:56:48 CET 2010.
 */

package de.htwk.autolat.Connector.types;
import java.util.List;

@SuppressWarnings("unused")
public class TaskDescription
{
    private final Documented<String> taskSampleConfig;
    private final ScoringOrder taskScoringOrder;
    
    public TaskDescription(Documented<String> taskSampleConfig,
                           ScoringOrder taskScoringOrder)
    {
        this.taskSampleConfig = taskSampleConfig;
        this.taskScoringOrder = taskScoringOrder;
    }
    
    public Documented<String> getTaskSampleConfig()
    {
        return taskSampleConfig;
    }
    
    public ScoringOrder getTaskScoringOrder()
    {
        return taskScoringOrder;
    }
    
    public String toString()
    {
        return "TaskDescription("
            + taskSampleConfig + ", "
            + taskScoringOrder + ")";
    }
    
    public boolean equals(Object other)
    {
        if (! (other instanceof TaskDescription))
            return false;
        TaskDescription oTaskDescription = (TaskDescription) other;
        if (!taskSampleConfig.equals(oTaskDescription.getTaskSampleConfig()))
            return false;
        if (!taskScoringOrder.equals(oTaskDescription.getTaskScoringOrder()))
            return false;
        return true;
    }
    
    public int hashCode()
    {
        return
            taskSampleConfig.hashCode() * 1 +
            taskScoringOrder.hashCode() * 37;
    }
    
}