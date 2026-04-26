const {setGlobalOptions} = require("firebase-functions");
const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const logger = require("firebase-functions/logger");
const {initializeApp} = require("firebase-admin/app");
const {getFirestore} = require("firebase-admin/firestore");
const {getMessaging} = require("firebase-admin/messaging");

initializeApp();
setGlobalOptions({maxInstances: 10});

exports.sendChatNotification = onDocumentCreated(
  {
    document: "chatrooms/{chatroomId}/chats/{messageId}",
    region: "europe-west1",
  },
  async (event) => {
    const messageData = event.data && event.data.data();

    if (!messageData) {
      logger.warn("No message data found");
      return;
    }

    const {chatroomId, messageId} = event.params;
    const senderId = messageData.senderId;
    const messageText = messageData.message;

    if (!senderId || !messageText) {
      logger.warn("Missing senderId or message", {chatroomId, messageId});
      return;
    }

    const db = getFirestore();
    const chatroomSnapshot = await db.doc(`chatrooms/${chatroomId}`).get();

    if (!chatroomSnapshot.exists) {
      logger.warn("Chatroom not found", {chatroomId});
      return;
    }

    const userIds = chatroomSnapshot.get("userIds") || [];
    const receiverId = userIds.find((userId) => userId !== senderId);

    if (!receiverId) {
      logger.warn("Receiver not found", {chatroomId, userIds});
      return;
    }

    const [senderSnapshot, receiverSnapshot] = await Promise.all([
      db.doc(`users/${senderId}`).get(),
      db.doc(`users/${receiverId}`).get(),
    ]);

    const senderName = senderSnapshot.get("username") || "New message";
    const receiverToken = receiverSnapshot.get("fcmToken");

    if (!receiverToken) {
      logger.warn("Receiver has no FCM token", {receiverId});
      return;
    }

    await getMessaging().send({
      token: receiverToken,
      notification: {
        title: senderName,
        body: messageText,
      },
      data: {
        chatroomId,
        senderId,
      },
    });

    logger.info("Notification sent", {chatroomId, messageId, receiverId});
  },
);
