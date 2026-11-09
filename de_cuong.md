# Đề cương tiểu luận

**Tên đề tài:** *Xây dựng quy trình DevSecOps an toàn với cơ chế triển khai không gián đoạn cho ứng dụng Web Java*

## 1. Mục tiêu và câu hỏi nghiên cứu

- **RQ1:** Thêm các cổng kiểm soát bảo mật (SAST, SCA, quét image, ký image) vào pipeline thì giảm được bao nhiêu lỗ hổng lọt lên production? Thời gian pipeline tăng thêm bao nhiêu?
- **RQ2:** Triển khai Blue-Green kèm health check và tự rollback cải thiện thời gian ngừng dịch vụ và khả năng phục hồi bao nhiêu so với cách hiện tại (Watchtower)?
- **Phạm vi:** Giữ nguyên ứng dụng vProfile, chỉ dùng một máy ảo Ubuntu qua Vagrant và GitHub Actions, không dùng Kubernetes.

## 2. Cấu trúc báo cáo

Các chương đánh dấu ♻ dùng lại từ niên luận, các chương ★ là phần mới.

| Chương | Nội dung |
|---|---|
| Mở đầu ♻★ | Bổ sung câu hỏi nghiên cứu, đóng góp và phương pháp nghiên cứu |
| I. Tổng quan và nghiên cứu liên quan ★ | Các nghiên cứu về DevSecOps, chuỗi cung ứng phần mềm (sự cố SolarWinds, Log4Shell), các chiến lược deployment. Kết thúc bằng bảng so sánh các nghiên cứu và chỉ ra khoảng trống mà đề tài giải quyết |
| II. Cơ sở lý thuyết ♻★ | Giữ phần CI/CD, Docker, GHCR, Watchtower. Thêm: shift-left security, SAST và SCA, SBOM, SLSA, Sigstore/Cosign, các chiến lược Recreate / Rolling / Blue-Green / Canary, các chỉ số DORA |
| III. Phân tích và thiết kế ★ | Phân tích rủi ro của pipeline cũ (mô hình đe dọa STRIDE), thiết kế pipeline mới và thiết kế luồng deploy |
| IV. Cài đặt ♻★ | Giữ phần Vagrant và compose. Thêm phần cài đặt các cổng bảo mật và cơ chế Blue-Green |
| V. Thực nghiệm và đánh giá ★ | 4 thực nghiệm ở mục 4, có bảng số liệu và biểu đồ |
| Kết luận ★ | Trả lời RQ1 và RQ2, nêu hạn chế và hướng phát triển (Kubernetes, GitOps, Canary) |

## 3. Thiết kế kỹ thuật

### 3a. Pipeline DevSecOps ([main.yml](.github/workflows/main.yml))

```
Build & Unit test ─┬─ SAST (CodeQL / SpotBugs+FindSecBugs)
 (JaCoCo)          ├─ SCA (OWASP Dependency-Check)        ── Gate 1
                   └─ Secret scan (Gitleaks)
        ↓
Build image amd64/arm64 → Trivy image scan (fail CRITICAL/HIGH) ── Gate 2
        ↓
Push by digest → SBOM (Syft) → Cosign sign + attest SBOM     ── Gate 3
        ↓
Deploy: verify chữ ký → Blue-Green
```

Các thay đổi cụ thể:
- Chuyển Trivy từ `exit-code: 0` sang `1`. Nếu đang có CVE tồn đọng, liệt kê chúng trong `.trivyignore` kèm lý do để còn theo dõi được.
- Checkstyle phải làm build thất bại khi vi phạm (`failOnViolation`).
- Tag image theo SHA, không dùng `latest` để deploy nữa.
- Bổ sung test thật cho [UserControllerTest](src/test/java/com/visualpathit/account/controllerTest/UserControllerTest.java). Hiện kết quả đang là `Tests run: 0`, đây là điểm hội đồng dễ hỏi nhất.

### 3b. Triển khai Blue-Green trên máy ảo

Hiện [docker-compose.yml](docker-compose.yml) đặt cứng `container_name: vproapp`, còn [nginvproapp.conf](Docker-files/web/nginvproapp.conf) trỏ cố định vào `vproapp:8080`. Cần đổi như sau:
- Tạo 2 service `vproapp-blue` và `vproapp-green`. File upstream của Nginx được sinh từ template, sau đó chạy `nginx -s reload`. Lệnh reload không làm rớt kết nối đang mở.
- Viết script `deploy.sh` chạy trên máy ảo, thực hiện lần lượt:
  1. `cosign verify` image theo digest.
  2. Khởi động màu đang rảnh.
  3. Chờ health check đạt (dùng `curl` vào trang login, thử N lần).
  4. Chuyển upstream sang màu mới.
  5. Theo dõi thêm khoảng 60 giây. Nếu lỗi thì chuyển lại màu cũ, tức là rollback.
- Cách kích hoạt `deploy.sh`: dùng **self-hosted runner** trên máy ảo. Cách này tránh phải mở SSH ra Internet, đồng thời giữ được tính chất pull-based giống Watchtower nên dễ so sánh. Watchtower được giữ lại làm **baseline** để đối chứng.

## 4. Thực nghiệm (Chương V)

| # | Thực nghiệm | Cách làm | Chỉ số đo |
|---|---|---|---|
| E1 | Hiệu quả cổng bảo mật | Cố tình đưa vào: một dependency có CVE (ví dụ log4j 2.14), một secret giả, một đoạn mã SQL injection, và một base image cũ | Tỉ lệ bị chặn ở từng gate, số false positive |
| E2 | Chi phí của các gate | Chạy pipeline cũ và mới, mỗi loại khoảng 15–20 lần | Thời gian trung bình ± độ lệch chuẩn của từng job |
| E3 | Downtime khi deploy | k6 bắn tải liên tục (ví dụ 50 VUs) trong lúc deploy, so sánh Watchtower với Blue-Green, mỗi loại 10 lần | Số request lỗi, khoảng downtime tính bằng giây, latency p95 |
| E4 | Tự phục hồi | Deploy một image hỏng (ứng dụng không khởi động được), và một image chưa ký | Blue-Green có tự rollback không và MTTR bao nhiêu; image chưa ký có bị từ chối không. Đối chiếu với Watchtower |

Kết quả dự kiến cần chứng minh bằng số liệu: Watchtower sẽ làm rớt request trong lúc restart và sẽ chạy cả image hỏng, còn Blue-Green không gây downtime và tự quay về bản cũ.

## 5. Kế hoạch 8 tuần

| Tuần | Công việc |
|---|---|
| 1 | Đọc tài liệu, viết Chương I, chốt câu hỏi nghiên cứu với giảng viên hướng dẫn |
| 2 | Viết test thật, thêm JaCoCo, SAST, SCA, Gitleaks |
| 3 | Trivy image gate, SBOM, ký image bằng Cosign |
| 4–5 | Blue-Green: tách compose, template Nginx, `deploy.sh`, self-hosted runner |
| 6 | Chạy E1–E4, thu số liệu |
| 7 | Viết Chương III–V, vẽ biểu đồ |
| 8 | Hoàn thiện, viết kết luận, chuẩn bị slide bảo vệ |

## 6. Tài liệu tham khảo nên có

- NIST SP 800-218 (SSDF)
- SLSA v1.0
- OWASP Top 10 CI/CD Security Risks
- DORA *Accelerate State of DevOps Report*
- Tài liệu của Sigstore/Cosign, Trivy, k6
- Martin Fowler, bài *BlueGreenDeployment*
- 3–5 bài báo khoa học về DevSecOps (ví dụ các bài survey trên IEEE hoặc ACM, tìm với từ khóa "DevSecOps systematic review") để làm phần nghiên cứu liên quan

Nên mang đề cương này gặp giảng viên hướng dẫn trước khi bắt tay làm, để chốt phạm vi.