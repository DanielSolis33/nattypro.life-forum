**7-7-26
**Designed and deployed a zero-downtime CI/CD pipeline for a Spring Boot app on EC2, with tailnet-secured deploys, externalized secrets, and Cloudflare-fronted origin lockdown.**
designing a CI/CD pipeline, GitHub Actions runners, artifact build-and-ship, systemd service management, externalized config/secrets separation 
**EC2, security groups, EBS/volume concepts, IAM roles (the SSM detour), Elastic IP, S3**
The Tailscale ACL work; tag ownership, src/dst grants, SSH policy rules, the tag-vs-user distinction. Layered in the security-group lockdown, 
Cloudflare origin protection, key-based auth, and reading auth.log to trace an intrusion-looking failure.**
**struggled getting this done because I lost shell access and had to work through Instance Connect, Session Manager, serial console, and landing on the security-group fix. 
The funny thing is I should have just checked the log from the getgo and it probably would have saved a ton a time setting this up.**
