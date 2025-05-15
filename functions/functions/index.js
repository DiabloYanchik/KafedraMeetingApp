const { onValueWritten } = require("firebase-functions/v2/database");
const { initializeApp } = require("firebase-admin/app");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

exports.notifyNewMeeting = onValueWritten(
  {
    ref: "/meetings/{meetingId}",
    // Укажите регион, если требуется, например: region: "europe-west1"
  },
  (event) => {
    // Проверяем, что это создание новой записи
    if (!event.data.after.exists()) {
      return null; // Если запись удалена, ничего не делаем
    }
    if (event.data.before.exists()) {
      return null; // Если запись обновлена, а не создана, ничего не делаем
    }

    const meeting = event.data.after.val();

    const payload = {
      notification: {
        title: "Новое заседание кафедры",
        body: `Тема: ${meeting.topic}, Дата: ${meeting.date}, Время: ${meeting.time}`,
      },
    };

    return getMessaging().sendToTopic("meetings", payload);
  }
);