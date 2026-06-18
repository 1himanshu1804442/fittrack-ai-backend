package com.example.demo.dto;

public class CustomWorkoutRequestDTO {
    private String targetMuscleGroup;
    private String equipmentAvailable;
    private String timeAvailable;
    private String experienceLevel;
    private String focus;

    // Getters and Setters
    public String getTargetMuscleGroup() { return targetMuscleGroup; }
    public void setTargetMuscleGroup(String targetMuscleGroup) { this.targetMuscleGroup = targetMuscleGroup; }

    public String getEquipmentAvailable() { return equipmentAvailable; }
    public void setEquipmentAvailable(String equipmentAvailable) { this.equipmentAvailable = equipmentAvailable; }

    public String getTimeAvailable() { return timeAvailable; }
    public void setTimeAvailable(String timeAvailable) { this.timeAvailable = timeAvailable; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public String getFocus() { return focus; }
    public void setFocus(String focus) { this.focus = focus; }
}
