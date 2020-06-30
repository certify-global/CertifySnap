package com.certify.snap.model;

import com.arcsoft.face.Face3DAngle;
import com.arcsoft.face.FaceShelterInfo;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;

public class FaceParameters {
    public int age = 0;
    public String gender = "";
    public int maskStatus = -2;
    public String faceShelter = "";
    public String face3DAngle = "";
    public String liveness = "";

    public String getGender(GenderInfo genderInfo) {
        String gender = "";
        if (genderInfo.getGender() == GenderInfo.MALE) {
            gender = "Male";
        } else if (genderInfo.getGender() == GenderInfo.FEMALE) {
            gender = "Female";
        } else if (genderInfo.getGender() == GenderInfo.UNKNOWN) {
            gender = "Unknown";
        }
        return gender;
    }

    public String getFaceShelter(FaceShelterInfo faceShelterInfo) {
        String faceShelter = "";
        if (faceShelterInfo.getFaceShelter() == FaceShelterInfo.NOT_SHELTERED) {
            faceShelter = "Not Sheltered";
        } else if (faceShelterInfo.getFaceShelter() == FaceShelterInfo.SHELTERED){
            faceShelter = "Sheltered";
        } else if (faceShelterInfo.getFaceShelter() == FaceShelterInfo.UNKNOWN) {
            faceShelter = "Unknown";
        }
        return faceShelter;
    }

    public String getFace3DAngle(Face3DAngle face3DAngleInfo) {
        String face3dAngle = "";
        face3dAngle = "(Yaw:" + face3DAngleInfo.getYaw() + " " +
                        "Pitch:" + face3DAngleInfo.getPitch() + " " +
                        "Roll:" + face3DAngleInfo.getRoll() + ")";
        return face3dAngle;
    }

    public String getFaceLiveness(LivenessInfo livenessInfo) {
        String liveness = "";
        switch (livenessInfo.getLiveness()) {
            case 0:
                liveness = "Not Alive";
            break;
            case 1:
                liveness = "Alive";
            break;
            case -1:
                liveness = "Unknown";
            break;
            case -2:
                liveness = "More than one face";
            break;
            case -3:
                liveness = "Face too small";
            break;
            case -4:
                liveness = "Face angle too large";
            break;
            case -5:
                liveness = "Face beyond boundary";
            break;
        }
        return liveness;
    }
}
