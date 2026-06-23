# Redstone Master Web

Companion-веб-приложение для Fabric-мода **Redstone Master** (Minecraft 1.21.11).

Spring Boot **3**, Java **21**, Thymeleaf, H2. Не входит в сборку мода — живёт в `other_projects/redstone-master-web/`.

При сборке Maven копирует языковые файлы, каталог обучения и текстуры из мода (`src/main/resources/assets/redstone-master/` в корне репозитория).

## Возможности

- **Главная** — описание мода (из языковых файлов), инструкция по установке
- **Обучение** — каталог разделов и уроков (как в моде), прогресс для авторизованных пользователей
- **Комментарии** к урокам, ответы, модерация
- **Профиль** — регистрация, вход, смена почты, аватар, прогресс уроков
- **Вход из мода** — OAuth-подобный обмен через `/mod-auth/*` и локальный callback
- **Новости** — публикация и редактирование (модераторы / админы)
- **Уведомления** — события модерации, mutes, комментарии
- **Модерация** — заявки на уроки, конвертер псевдо-видео, mute / unmute пользователей
- **Администрирование** — очередь уроков, unmute модераторов, статистика
- **Справочник настроек** — `/settings`, синхронизирован с настройками мода

## Требования

- JDK 21
- Maven 3.9+
- **FFmpeg** — для конвертера псевдо-видео (модераторы)
- Опционально: SMTP для регистрации по e-mail (`application-local.properties`)

## Быстрый старт

```bash
cd other_projects/redstone-master-web
mvn spring-boot:run
```

Windows:

```bat
other_projects\redstone-master-web\run.bat
```

Сайт: http://localhost:8080  
Язык: `?lang=en` (по умолчанию `ru`).

### Связь с модом

1. Запустите веб-приложение.
2. В `config/redstone-master.json` мода укажите `"webBaseUrl"`:
   - production: `"https://redstone-master.ru"`
   - локально: `"http://localhost:8080"`
3. В моде: вкладка **Профиль** → **Войти** или **Регистрация**.

Подробнее: [`docs/DOMAIN_SETUP.md`](../docs/DOMAIN_SETUP.md) в корне репозитория мода.

Пример локальных настроек: `application-local.properties.example` → `application-local.properties`.  
Production: `application-production-local.properties.example` + профиль `production`.  
**Railway:** [`docs/RAILWAY_DEPLOY.md`](../docs/RAILWAY_DEPLOY.md) + `railway.env.example`.

## Страницы

| URL | Доступ | Описание |
|-----|--------|----------|
| `/` | Все | Главная, о моде, установка |
| `/tutorial` | Все | Каталог обучения |
| `/tutorial/{sectionId}/{lessonId}` | Все | Урок, комментарии |
| `/settings` | Все | Справочник настроек и клавиш мода |
| `/profile` | Все | Профиль, вход, регистрация |
| `/news` | Все | Новости |
| `/notifications` | USER+ | Уведомления |
| `/moderation` | MODERATOR+ | Панель модератора |
| `/moderation/pseudo-video` | MODERATOR+ | Конвертер видео → PNG-кадры |
| `/moderation/lesson` | MODERATOR+ | Редактор урока |
| `/moderation/submissions` | MODERATOR+ | Заявки на публикацию |
| `/admin` | ADMIN | Панель администратора |
| `/admin/lesson-submissions` | ADMIN | Очередь уроков на проверку |

## Роли

| Роль | Возможности |
|------|-------------|
| **USER** | Профиль, прогресс, комментарии |
| **MODERATOR** | Новости, модерация комментариев, mute пользователей, заявки на уроки, псевдо-видео |
| **ADMIN** | Всё выше + unmute модераторов, удаление любых комментариев, очередь уроков |

## Псево-видео для мода

Мод воспроизводит **последовательность PNG-кадров** (15 fps, 854×480 по умолчанию), а не обычное видео.

**Модерация → Конвертер псевдо-видео:**

1. Задайте `video id` (имя папки в ресурсах мода).
2. Загрузите исходное видео — сервер вызывает **FFmpeg** и создаёт `frame_00000.png`, …, `meta.json`.
3. Скачайте **JAR** для `mods` или **ZIP** с кадрами.

Настройки в `application.properties`:

```properties
app.moderation.ffmpeg-executable=ffmpeg
app.moderation.video-fps=15
app.moderation.video-width=854
app.moderation.video-height=480
```

На Windows укажите полный путь к `ffmpeg.exe`, если он не в `PATH`.

## REST API

| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/api/info` | Версия мода и ссылки |
| GET | `/api/tutorial/sections?lang=ru` | Каталог обучения |
| GET | `/api/settings?lang=ru` | Справочник настроек |
| GET | `/api/keys?lang=ru` | Привязки клавиш |
| GET | `/api/tutorial/{section}/{lesson}/comments` | Комментарии (для мода) |
| POST | `/mod-auth/*` | Авторизация из мода |

## Сборка

```bash
mvn clean package
java -jar target/redstone-master-web-1.0.0.jar
```

## Структура

```
src/main/java/ru/redstonemaster/web/
  controller/       — страницы и REST
  modauth/          — вход из мода
  comment/          — комментарии к урокам
  moderation/       — заявки, псевдо-видео, JAR уроков
  notification/     — уведомления
  profile/          — профиль, аватар
  user/             — регистрация, роли
  service/          — справочники, контент мода
  locale/           — ru / en

src/main/resources/
  templates/        — Thymeleaf
  static/css/       — стили
```

## Справочник настроек мода (актуально)

| Раздел | Настройка | По умолчанию |
|--------|-----------|--------------|
| Интерфейс | Размер окна мода | 80% |
| Интерфейс | Прозрачность фона | 75% (ползунок 0–100%) |
| Интерфейс | Пауза при открытии | Вкл |
| Интерфейс | Высокий контраст рамок | Выкл |
| Интерфейс | Автоподбор языка | Вкл |
| Интерфейс | Выбор языка | Язык Minecraft |
| Управление | Сохранять вкладку и позицию | Вкл |
| Управление | Закрывать мод по повторному нажатию | Вкл |
| Обучение | Сворачивать другие разделы | Выкл |

Подробности и подсказки: http://localhost:8080/settings

## Лицензия

См. корень репозитория мода — [CC0-1.0](../../LICENSE).
