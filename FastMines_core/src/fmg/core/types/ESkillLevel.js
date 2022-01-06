const /* enum */ ESkillLevel = {
    eBeginner: { ordinal: 0 },
    eAmateur : { ordinal: 1 },
    eProfi   : { ordinal: 2 },
    eCrazy   : { ordinal: 3 },
    eCustom  : { ordinal: 4 },

    getName: function(/* ESkillLevel */ skill) {
        if (skill == null)
            return 'typeESkillLevel';
        for (var e in ESkillLevel) {
            var val = ESkillLevel[e];
            if (val === skill)
                return e;
        }
        throw new Error('Bad argument skill: value is ' + skill);
    }

};
