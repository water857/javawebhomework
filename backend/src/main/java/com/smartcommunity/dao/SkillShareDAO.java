package com.smartcommunity.dao;

import com.smartcommunity.entity.SkillShare;

import java.util.List;

public interface SkillShareDAO {
    int createSkill(SkillShare skill);

    List<SkillShare> getSkills();
}
