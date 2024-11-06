package SEproject.service;

import SEproject.domain.Member;
import SEproject.dto.LoginMemberDTO;
import SEproject.dto.NewMemberDTO;
import SEproject.repository.EpicRepository;
import SEproject.repository.MemberRepository;
import SEproject.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final EpicRepository epicRepository;

    @Autowired
    public MemberService(MemberRepository memberRepository, ProjectRepository projectRepository, EpicRepository epicRepository) {
        this.memberRepository = memberRepository;
        this.projectRepository = projectRepository;
        this.epicRepository = epicRepository;
    }

    public Member createMember(NewMemberDTO newMemberDTO) {
        return memberRepository.save(newMemberDTO);
    }

    public Member login(LoginMemberDTO loginMemberDTO) {
        return memberRepository.findByEmailId(loginMemberDTO.getEmailId())
                .filter(m -> m.getPassword().equals(loginMemberDTO.getPassword()))
                .orElse(null);
    }

    public Map<String, Map<String, Long>> projectList(Long memberId) {
        // 해당 Member의 ProjectId 추출
        List<Long> projectIds = memberRepository.findById(memberId).getProjectIds();
        Map<String, Map<String, Long>> result = new HashMap<>();

        // 프로젝트 별 - 완료한 에픽/ 전체 에픽 수
        for(int i = 0; i < projectIds.size(); i++) {
            Long projectId = projectIds.get(i);
            Map<String, Long> epicRatio = new HashMap<>();

            // 전체 에픽 수 구하기
            List<Long> epicsId = projectRepository.findById(projectId).getEpicsId();
            epicRatio.put("totalEpics", Long.valueOf(epicsId.size()));

            // 완료한 에픽 수 구하기
            Long completedEpics = 0L;
            for(int j = 0; j < epicsId.size(); j++) {
                if(epicRepository.findById(epicsId.get(j)).getIsCompleted()) {
                    completedEpics++;
                }
            }
            epicRatio.put("completedEpics", completedEpics);

            result.put(projectRepository.findById(projectId).getProjectName(), epicRatio);
        }

        return result;
    }
}
