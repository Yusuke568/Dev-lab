package com.example.application.port.in;

import com.example.entity.Classmaster;
import java.util.List;

/**
 * 縺吶∋縺ｦ縺ｮ蠖ｹ閨ｷ諠・ｱ繧貞叙蠕励☆繧九◆繧√・繝ｦ繝ｼ繧ｹ繧ｱ繝ｼ繧ｹ縺ｮ蜈･蜉帙・繝ｼ繝医・
 */
public interface GetAllClassmastersUseCase {

    /**
     * 縺吶∋縺ｦ縺ｮ蠖ｹ閨ｷ諠・ｱ繧偵Μ繧ｹ繝医→縺励※蜿門ｾ励＠縺ｾ縺吶・
     * @return 蠖ｹ閨ｷ諠・ｱ縺ｮ繝ｪ繧ｹ繝・
     */
    List<Classmaster> getAllClassmasters();
}
