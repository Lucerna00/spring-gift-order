package gift.service;

import gift.domain.Member;
import gift.dto.MemberDTO;
import gift.dto.MemberPasswordDTO;
import gift.exception.AlreadyExistMemberException;
import gift.exception.InvalidPasswordException;
import gift.exception.NoSuchMemberException;
import gift.repository.MemberRepository;
import gift.util.JwtProvider;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    @Autowired
    public MemberService(MemberRepository memberRepository, JwtProvider jwtProvider) {
        this.memberRepository = memberRepository;
        this.jwtProvider = jwtProvider;
    }

    public MemberDTO findMember(String email) {
        return memberRepository.findById(email)
            .orElseThrow(NoSuchMemberException::new)
            .toDTO();
    }

    public void register(MemberDTO memberDTO) {
        try {
            findMember(memberDTO.email());
            throw new AlreadyExistMemberException();
        } catch (NoSuchMemberException e) {
            memberRepository.save(memberDTO.toEntity()).toDTO();
        }
    }

    public Map<String, String> login(MemberDTO memberDTO) {
        MemberDTO foundMemberDTO = findMember(memberDTO.email());
        checkPassword(memberDTO.password(), foundMemberDTO.password());
        return Map.of("token:", jwtProvider.createAccessToken(memberDTO));
    }

    public Map<String, String> changePassword(MemberDTO memberDTO, MemberPasswordDTO memberPasswordDTO) {
        checkPassword(memberPasswordDTO.password(), memberDTO.password());
        Member member = new Member(memberDTO.email(), memberPasswordDTO.newPassword1());
        MemberDTO updatedMemberDTO = memberRepository.save(member).toDTO();
        return Map.of("token:", jwtProvider.createAccessToken(updatedMemberDTO));
    }

    private void checkPassword(String password, String expectedPassword) {
        if (!password.equals(expectedPassword)){
            throw new InvalidPasswordException();
        }
    }
}
