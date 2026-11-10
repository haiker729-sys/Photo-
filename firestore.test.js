const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_photographer_1";
const BOB_UID = "bob_photographer_2";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function sampleEventPayload(userId) {
  const now = new Date();
  return {
    userId,
    name: "Royal Orchid Wedding Gala",
    category: "Wedding",
    eventDate: "2026-11-24",
    location: "Grand Ballroom, Mumbai",
    coverType: "wedding",
    coverUri: "",
    status: "Active",
    qrCodeValue: "https://jummophotos.ai/e/evt_101",
    qrEnabled: true,
    qrExpirationDate: "2027-01-15",
    guestAccessMode: "FaceSearchOnly",
    guestAllowDownload: true,
    totalPhotos: 0,
    uploadedPhotos: 0,
    pendingPhotos: 0,
    failedPhotos: 0,
    driveSyncStatus: "NotConnected",
    createdAt: now,
    updatedAt: now,
  };
}

function sampleUploadPayload(userId, eventId) {
  const now = new Date();
  return {
    userId,
    eventId,
    eventName: "Royal Orchid Wedding Gala",
    fileName: "DSC_0412.JPG",
    localUri: "content://media/external/images/media/101",
    sourceType: "DSLRImport",
    uploadMode: "Automatic",
    status: "Pending",
    progress: 0,
    fileSizeBytes: 4200000,
    mimeType: "image/jpeg",
    faceIndexStatus: "AwaitingBackend",
    driveFileId: "",
    errorMessage: "",
    createdAt: now,
    updatedAt: now,
  };
}

test("Unauthenticated user: cannot read events or uploads", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("events").get());
  await assertFails(unauthDb.collection("uploads").get());
});

test("Authenticated photographer: can create and query their own event", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("events").doc("event_1").set(sampleEventPayload(ALICE_UID))
  );
  await assertSucceeds(
    aliceDb.collection("events").where("userId", "==", ALICE_UID).get()
  );
});

test("Authenticated photographer: fails query without userId filter", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("events").get());
});

test("Cross-user isolation: Bob cannot read or update Alice's event", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("events")
      .doc("alice_event")
      .set(sampleEventPayload(ALICE_UID));
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("events").doc("alice_event").get());
  await assertFails(
    bobDb.collection("events").doc("alice_event").update({ name: "Hijacked" })
  );
});

test("Shadow update test: rejects unexpected ghost field on event create", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const payload = {
    ...sampleEventPayload(ALICE_UID),
    isVerifiedGhostField: true,
  };
  await assertFails(aliceDb.collection("events").doc("event_ghost").set(payload));
});

test("Role escalation guard: regular user cannot create profile with admin role", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@example.com",
      studioName: "Alice Studio",
      role: "admin",
      driveConnected: false,
      driveFolderName: "Jummo_Events",
      autoUploadEnabled: true,
      wifiOnlyUpload: true,
      batteryNotLowUpload: true,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Authenticated photographer: can create, pause, reassign event, and complete upload record", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const payload = {
    ...sampleUploadPayload(ALICE_UID, "event_1"),
    sourceType: "GoogleDriveImport",
  };
  await assertSucceeds(
    aliceDb
      .collection("uploads")
      .doc("upload_1")
      .set(payload)
  );
  await assertSucceeds(
    aliceDb.collection("uploads").doc("upload_1").update({
      status: "Paused",
      eventId: "event_2",
      eventName: "Mumbai Reception Gala",
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("uploads").doc("upload_1").update({
      status: "Uploaded",
      progress: 100,
      updatedAt: new Date(),
    })
  );
});

function sampleBannerPayload(userId, displayOrder = 0) {
  const now = new Date();
  return {
    userId,
    imageUri: "preset://wedding_cover",
    storagePath: "banners/banner_wedding_1.jpg",
    displayOrder,
    isActive: true,
    slideDurationSeconds: 4,
    createdAt: now,
    updatedAt: now,
  };
}

test("HomeBanner: authenticated admin/owner can create, reorder, toggle active, and query banners", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb
      .collection("home_banners")
      .doc("banner_1")
      .set(sampleBannerPayload(ALICE_UID, 0))
  );
  await assertSucceeds(
    aliceDb.collection("home_banners").doc("banner_1").update({
      displayOrder: 2,
      isActive: false,
      slideDurationSeconds: 5,
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("home_banners").where("userId", "==", ALICE_UID).get()
  );
});

test("HomeBanner: rejects cross-user access and ghost fields", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("home_banners")
      .doc("alice_banner")
      .set(sampleBannerPayload(ALICE_UID, 0));
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("home_banners").doc("alice_banner").get());
  await assertFails(
    bobDb
      .collection("home_banners")
      .doc("alice_banner")
      .update({ isActive: false, updatedAt: new Date() })
  );

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("home_banners").doc("ghost_banner").set({
      ...sampleBannerPayload(ALICE_UID, 1),
      unexpectedOverlayText: "Forbidden",
    })
  );
});

function sampleWebsiteCustomizationPayload(userId, eventId = "event_1") {
  const now = new Date();
  return {
    userId,
    eventId,
    eventName: "Royal Orchid Wedding Gala",
    brandingEnabled: true,
    studioName: "Jummo Pro Studio",
    photographerName: "Meraj Sir",
    logoUri: "",
    profileImageUri: "",
    coverImageUri: "preset://wedding_cover",
    bannerUrisCsv: "preset://wedding_cover|preset://hero_studio",
    autoSliderEnabled: true,
    sliderIntervalSeconds: 4,
    bannerHeightMode: "Standard",
    bannerCropMode: "CenterCrop",
    theme: "RoyalPurple",
    primaryAccentHex: "#8B20E8",
    secondaryAccentHex: "#F7C747",
    fontFamily: "PlayfairDisplay",
    layout: "Editorial",
    welcomeMessage: "Welcome to our curated event gallery.",
    contactEmail: "merajalam906090@gmail.com",
    contactPhone: "+91 98765 43210",
    studioLocation: "Mumbai & New Delhi, India",
    instagramUrl: "https://instagram.com/jummophotosai",
    portfolioUrl: "https://haiker729-sys.github.io/Photo-/",
    faceSearchEnabled: true,
    galleryEnabled: true,
    downloadsEnabled: true,
    shareEnabled: true,
    guestAccessMode: "FaceSearchOnly",
    publicationStatus: "Published",
    publishedVersion: 1,
    publishedUrl: "https://haiker729-sys.github.io/Photo-/?event=event_1",
    hasUnpublishedChanges: false,
    adminAuditNote: "",
    createdAt: now,
    updatedAt: now,
  };
}

test("WebsiteCustomization: authenticated owner can save draft, publish, and query customizations", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb
      .collection("website_customizations")
      .doc("web_event_1")
      .set(sampleWebsiteCustomizationPayload(ALICE_UID, "event_1"))
  );
  await assertSucceeds(
    aliceDb.collection("website_customizations").doc("web_event_1").update({
      theme: "MidnightBlack",
      layout: "Masonry",
      publishedVersion: 2,
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(
    aliceDb
      .collection("website_customizations")
      .where("userId", "==", ALICE_UID)
      .get()
  );
});

test("WebsiteCustomization & WebsiteVersionSnapshot: enforces cross-user isolation and immutable version snapshots", async () => {
  const now = new Date();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("website_versions").doc("ver_event_1_v1").set({
      userId: ALICE_UID,
      eventId: "event_1",
      customizationId: "web_event_1",
      versionNumber: 1,
      studioName: "Jummo Pro Studio",
      photographerName: "Meraj Sir",
      logoUri: "",
      coverImageUri: "preset://wedding_cover",
      bannerUrisCsv: "preset://wedding_cover",
      theme: "RoyalPurple",
      layout: "Editorial",
      welcomeMessage: "Welcome",
      guestAccessMode: "FaceSearchOnly",
      faceSearchEnabled: true,
      galleryEnabled: true,
      downloadsEnabled: true,
      publishedUrl: "https://haiker729-sys.github.io/Photo-/?event=event_1",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Version snapshots are immutable once published
  await assertFails(
    aliceDb.collection("website_versions").doc("ver_event_1_v1").update({
      versionNumber: 99,
    })
  );

  // Cross-user Bob cannot read or mutate Alice's website customization
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("website_customizations")
      .doc("web_alice_1")
      .set(sampleWebsiteCustomizationPayload(ALICE_UID, "event_1"));
  });
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("website_customizations").doc("web_alice_1").get()
  );
});

