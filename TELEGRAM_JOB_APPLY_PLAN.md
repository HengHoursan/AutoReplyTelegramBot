# 📋 Telegram Recruitment Bot - Architecture & Guide

This project is a lightweight Java backend that listens to candidates applying through your Telegram channel, saves their CV files, auto-replies to the candidate, and instantly forwards the CV + alert directly to the Recruiter's personal Telegram chat.

---

## 1. System Flow

```
[ Telegram Channel Post (Manual) ]
  └── Link: https://t.me/TestAuto_ReplyBot?start=JOB_ID
        │
        ▼ (User clicks link)
[ Telegram Bot 1-on-1 Chat ]
  ├── User taps START
  ├── Bot asks for CV file (PDF / DOCX)
  └── Candidate drops / attaches CV file
        │
        ▼ (Document received)
[ Java Backend (IntelliJ IDEA) ]
  ├── 1. Saves CV file to ./received_cvs/ (keeps original filename)
  ├── 2. Sends confirmation auto-reply to Candidate
  └── 3. Forwards CV file & alert directly to Recruiter's Telegram!
```

---

## 2. Configuration (`config.properties`)

```properties
bot.token=YOUR_BOT_TOKEN_FROM_BOTFATHER
bot.username=YOUR_BOT_USERNAME
channel.id=@your_channel_username
save.directory=received_cvs
api.base.url=https://your-worker.your-subdomain.workers.dev

# Recruiter Telegram User ID (get from @userinfobot)
recruiter.chat.id=YOUR_NUMERIC_ID
```

---

## 3. How to Set Recruiter Notifications

1. Have the recruiter open Telegram and message **[@userinfobot](https://t.me/userinfobot)**.
2. It replies with their numeric ID (e.g. `123456789`).
3. Have the recruiter click **START** on **[@TestAuto_ReplyBot](https://t.me/TestAuto_ReplyBot)** once (gives the bot permission to message them).
4. Put the numeric ID in `config.properties`:
   `recruiter.chat.id=123456789`
5. Restart the bot in IntelliJ!

---

## 4. Templates (`templates/`)

All messages are clean `.html` templates with `{{placeholder}}` variables:

- `welcome.html`: General greeting when candidate opens bot.
- `welcome_job.html`: Greeting mentioning the specific job title from `?start=...`.
- `cv_received.html`: Confirmation sent to the candidate upon receiving their CV.
- `recruiter_notification.html`: Alert sent to the Recruiter with candidate info & forwarded CV.
- `please_attach.html`: Prompt sent when candidate types text instead of attaching a file.
- `sent_photo.html`: Prompt sent when candidate uploads a photo instead of a document.
