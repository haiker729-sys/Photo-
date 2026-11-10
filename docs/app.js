/**
 * Jummo Photos AI — Official Website & Live GitHub Release APK Verification Engine
 * Repository: https://github.com/haiker729-sys/Photo-
 * Official Website: https://haiker729-sys.github.io/Photo-/
 */
(function () {
  const GITHUB_OWNER = 'haiker729-sys';
  const GITHUB_REPO = 'Photo-';
  const REPO_URL = `https://github.com/${GITHUB_OWNER}/${GITHUB_REPO}`;
  const RELEASES_PAGE_URL = `${REPO_URL}/releases`;
  const LATEST_RELEASE_API = `https://api.github.com/repos/${GITHUB_OWNER}/${GITHUB_REPO}/releases/latest`;
  const ALL_RELEASES_API = `https://api.github.com/repos/${GITHUB_OWNER}/${GITHUB_REPO}/releases`;

  // App Preview Screens Metadata (All 8 Implemented Screens)
  const APP_SCREENS = [
    {
      id: 'login',
      number: 'Screen 01',
      title: 'Luxury Studio Login Screen',
      badge: 'Firebase Auth + Google Sign-In',
      image: './assets/screen-login.png',
      description:
        'Features the DSLR bokeh hero header, golden camera lens emblem, 5 event categories (Weddings, Festivals, Corporate, Schools, Live Events), 4 purple glass feature rows, and official Google Sign-In via Jetpack Credential Manager.'
    },
    {
      id: 'home',
      number: 'Screen 02',
      title: 'Home Dashboard',
      badge: 'Live Telemetry & Quick Actions',
      image: './assets/screen-suite-8.png',
      description:
        'Displays real-time counts for Events, Photos, Uploaded, Pending, and Failed items, side-by-side + Create Event and Camera actions, Google Drive sync indicator, local storage bar, and Recent Events.'
    },
    {
      id: 'events',
      number: 'Screen 03',
      title: 'My Events Manager',
      badge: 'Category & Active/Archived Filters',
      image: './assets/screen-suite-8.png',
      description:
        'Organize Wedding, Birthday, Festival, School, and Corporate shoots with instant search, Active/Archived tabs, cover photos, and 1-tap Gallery, QR, Edit, Archive, and Delete controls.'
    },
    {
      id: 'camera',
      number: 'Screen 04',
      title: 'CameraX Pro Viewfinder',
      badge: 'Rule-of-Thirds • HDR • 4K Framing',
      image: './assets/screen-suite-8.png',
      description:
        'Full-bleed CameraX viewfinder with top Flash, HDR, 4K, and Grid controls, floating active event selector pill, front/rear lens switching, and local original storage in filesDir/photos.'
    },
    {
      id: 'uploads',
      number: 'Screen 05',
      title: 'Smart Upload Queue & DSLR Import',
      badge: 'WorkManager • Wi-Fi & Battery Guard',
      image: './assets/screen-suite-8.png',
      description:
        'Monitor Queue, Uploaded, and Failed transfers with percentage progress bars, toggle Automatic Upload with Wi-Fi Only and Battery > 20% protection, or import RAW/DNG/CR2/NEF/JPG from DSLR SD cards.'
    },
    {
      id: 'profile',
      number: 'Screen 06',
      title: 'Profile & Studio Settings',
      badge: 'Photographer Pro Workspace',
      image: './assets/screen-suite-8.png',
      description:
        'Manage Studio Display Name, Google Drive folder bridge, Upload Preferences, Local Original Storage metrics, Guest Face-Search Consent privacy sessions, Camera permissions, and Admin access.'
    },
    {
      id: 'gallery',
      number: 'Screen 07',
      title: 'Event Photo Gallery & Guest Portal',
      badge: 'All Photos • People (AI) • Highlights',
      image: './assets/screen-suite-8.png',
      description:
        '3-column responsive event photo gallery with All Photos, People (AI), and Highlights tabs, multi-select batch actions, QR sharing, and consent-first Guest AI Face Search.'
    },
    {
      id: 'admin',
      number: 'Screen 08',
      title: 'Multi-Role Admin Dashboard',
      badge: 'KPIs • Banner Slider • Website Audit',
      image: './assets/screen-suite-8.png',
      description:
        'Role-based command center with 2x2 KPI cards, image-only Home Banner slider management, Website Management & Suspension Audit, dual-tone storage bar, 7-day upload chart, and 15 interactive modules.'
    },
    {
      id: 'website_customizer',
      number: 'Screen 09',
      title: 'Photographer Website Customizer',
      badge: 'Royal Purple • Editorial Layout • Live Preview',
      image: './assets/screen-suite-8.png',
      description:
        'Full Website Builder enabling photographers to customize studio branding, clean image-only hero banners, Royal Purple & luxury themes, Editorial / Classic Grid / Masonry galleries, AI Face Search permissions, Save Draft, Publish & Version Rollback.'
    }
  ];

  // State for verified GitHub Release
  let verifiedApkState = {
    checked: false,
    available: false,
    downloadUrl: null,
    assetName: null,
    version: 'APK release is being prepared.',
    sizeText: 'APK release is being prepared.',
    updatedText: 'APK release is being prepared.',
    releaseHtmlUrl: RELEASES_PAGE_URL,
    errorReason: 'APK release is being prepared.'
  };

  function formatBytes(bytes) {
    if (!bytes || isNaN(bytes) || bytes <= 0) return 'Verified APK';
    const mb = bytes / (1024 * 1024);
    return `${mb.toFixed(2)} MB`;
  }

  function formatDate(isoStr) {
    if (!isoStr) return 'Published on GitHub';
    try {
      const date = new Date(isoStr);
      return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      });
    } catch (_) {
      return isoStr;
    }
  }

  function extractApkAssetFromRelease(releaseObj) {
    if (!releaseObj || !Array.isArray(releaseObj.assets)) return null;
    // Prefer versioned Jummo-Photos-AI-*.apk first, then any .apk
    const preferred = releaseObj.assets.find(
      (asset) =>
        asset &&
        typeof asset.name === 'string' &&
        asset.name.toLowerCase().startsWith('jummo-photos-ai') &&
        asset.name.toLowerCase().endsWith('.apk') &&
        asset.browser_download_url
    );
    if (preferred) return preferred;
    return (
      releaseObj.assets.find(
        (asset) =>
          asset &&
          typeof asset.name === 'string' &&
          asset.name.toLowerCase().endsWith('.apk') &&
          asset.browser_download_url
      ) || null
    );
  }

  async function verifyGitHubReleaseApk() {
    const statusPill = document.getElementById('apk-release-status-pill');
    const versionEl = document.getElementById('apk-meta-version');
    const sizeEl = document.getElementById('apk-meta-size');
    const updatedEl = document.getElementById('apk-meta-updated');
    const linkTextEl = document.getElementById('apk-meta-link-text');
    const downloadBtns = document.querySelectorAll('.js-apk-download-trigger');
    const errorBox = document.getElementById('apk-download-error-box');
    const errorText = document.getElementById('apk-download-error-text');

    try {
      let releaseData = null;
      let apkAsset = null;

      // 1. Primary check: https://api.github.com/repos/haiker729-sys/Photo-/releases/latest
      const latestResp = await fetch(LATEST_RELEASE_API, {
        headers: { Accept: 'application/vnd.github+json' }
      });

      if (latestResp.ok) {
        releaseData = await latestResp.json();
        apkAsset = extractApkAssetFromRelease(releaseData);
      }

      // 2. Fallback check across releases list if latest returned 404 or had no .apk asset
      if (!apkAsset) {
        const listResp = await fetch(ALL_RELEASES_API, {
          headers: { Accept: 'application/vnd.github+json' }
        });
        if (listResp.ok) {
          const releases = await listResp.json();
          if (Array.isArray(releases)) {
            for (const rel of releases) {
              const candidate = extractApkAssetFromRelease(rel);
              if (candidate) {
                releaseData = rel;
                apkAsset = candidate;
                break;
              }
            }
          }
        }
      }

      if (releaseData && apkAsset) {
        verifiedApkState = {
          checked: true,
          available: true,
          downloadUrl: apkAsset.browser_download_url,
          assetName: apkAsset.name || 'Jummo-Photos-AI.apk',
          version: releaseData.tag_name || releaseData.name || 'v1.0.0',
          sizeText: formatBytes(apkAsset.size),
          updatedText: formatDate(apkAsset.updated_at || releaseData.published_at),
          releaseHtmlUrl: releaseData.html_url || RELEASES_PAGE_URL,
          errorReason: null
        };

        if (statusPill) {
          statusPill.className = 'release-status-pill verified';
          statusPill.textContent = `✓ Verified GitHub Release APK (${verifiedApkState.version})`;
        }
        if (versionEl) versionEl.textContent = verifiedApkState.version;
        if (sizeEl) sizeEl.textContent = verifiedApkState.sizeText;
        if (updatedEl) updatedEl.textContent = verifiedApkState.updatedText;
        if (linkTextEl) {
          linkTextEl.innerHTML = `<a href="${verifiedApkState.downloadUrl}" style="color: var(--gold-bright); text-decoration: underline;">${verifiedApkState.assetName}</a>`;
        }
        if (errorBox) errorBox.classList.remove('visible');

        downloadBtns.forEach((btn) => {
          btn.setAttribute('href', verifiedApkState.downloadUrl);
          btn.setAttribute('download', verifiedApkState.assetName);
        });
      } else {
        verifiedApkState = {
          checked: true,
          available: false,
          downloadUrl: null,
          assetName: null,
          version: 'APK release is being prepared.',
          sizeText: 'Pending release asset',
          updatedText: 'Pending release run',
          releaseHtmlUrl: RELEASES_PAGE_URL,
          errorReason: 'APK release is being prepared.'
        };

        if (statusPill) {
          statusPill.className = 'release-status-pill unavailable';
          statusPill.textContent = 'APK release is being prepared.';
        }
        if (versionEl) versionEl.textContent = 'APK release is being prepared.';
        if (sizeEl) sizeEl.textContent = verifiedApkState.sizeText;
        if (updatedEl) updatedEl.textContent = verifiedApkState.updatedText;
        if (linkTextEl) linkTextEl.textContent = 'APK release is being prepared.';
        if (errorBox) errorBox.classList.add('visible');
        if (errorText) errorText.textContent = 'APK release is being prepared.';
      }
    } catch (_) {
      verifiedApkState = {
        checked: true,
        available: false,
        downloadUrl: null,
        assetName: null,
        version: 'APK release is being prepared.',
        sizeText: 'Check GitHub Releases',
        updatedText: 'Check GitHub Releases',
        releaseHtmlUrl: RELEASES_PAGE_URL,
        errorReason: 'APK release is being prepared.'
      };
      if (statusPill) {
        statusPill.className = 'release-status-pill unavailable';
        statusPill.textContent = 'APK release is being prepared.';
      }
      if (versionEl) versionEl.textContent = 'APK release is being prepared.';
      if (sizeEl) sizeEl.textContent = verifiedApkState.sizeText;
      if (updatedEl) updatedEl.textContent = verifiedApkState.updatedText;
      if (linkTextEl) linkTextEl.textContent = 'APK release is being prepared.';
      if (errorBox) errorBox.classList.add('visible');
      if (errorText) errorText.textContent = 'APK release is being prepared.';
    }
  }

  function setupApkDownloadHandlers() {
    const downloadBtns = document.querySelectorAll('.js-apk-download-trigger');
    const errorBox = document.getElementById('apk-download-error-box');
    const errorText = document.getElementById('apk-download-error-text');

    downloadBtns.forEach((btn) => {
      btn.addEventListener('click', function (e) {
        if (verifiedApkState.available && verifiedApkState.downloadUrl) {
          return;
        }
        e.preventDefault();
        const apkSection = document.getElementById('download-apk');
        if (apkSection) {
          apkSection.scrollIntoView({ behavior: 'smooth' });
        }
        if (errorBox) {
          errorBox.classList.add('visible');
        }
        if (errorText) {
          errorText.textContent = 'APK release is being prepared.';
        }
      });
    });
  }

  function setupScreenPreviewGallery() {
    const tabsContainer = document.getElementById('preview-tabs-container');
    const miniGrid = document.getElementById('screens-mini-grid');
    const activeImg = document.getElementById('preview-active-image');
    const activeNum = document.getElementById('preview-active-number');
    const activeTitle = document.getElementById('preview-active-title');
    const activeBadge = document.getElementById('preview-active-badge');
    const activeDesc = document.getElementById('preview-active-desc');

    if (!tabsContainer || !miniGrid) return;

    function selectScreen(screenId) {
      const target = APP_SCREENS.find((s) => s.id === screenId) || APP_SCREENS[0];
      if (activeImg) {
        activeImg.src = target.image;
        activeImg.alt = target.title;
      }
      if (activeNum) activeNum.textContent = target.number;
      if (activeTitle) activeTitle.textContent = target.title;
      if (activeBadge) activeBadge.textContent = target.badge;
      if (activeDesc) activeDesc.textContent = target.description;

      document.querySelectorAll('.preview-tab-btn').forEach((btn) => {
        btn.classList.toggle('active', btn.dataset.screen === target.id);
      });
      document.querySelectorAll('.screen-mini-card').forEach((card) => {
        card.classList.toggle('active', card.dataset.screen === target.id);
      });
    }

    tabsContainer.innerHTML = APP_SCREENS.map(
      (s, idx) =>
        `<button type="button" class="preview-tab-btn ${idx === 0 ? 'active' : ''}" data-screen="${s.id}">${s.title}</button>`
    ).join('');

    miniGrid.innerHTML = APP_SCREENS.map(
      (s, idx) => `
        <div class="screen-mini-card ${idx === 0 ? 'active' : ''}" data-screen="${s.id}">
          <div class="screen-num">${s.number}</div>
          <h4>${s.title}</h4>
          <p>${s.badge}</p>
        </div>
      `
    ).join('');

    tabsContainer.addEventListener('click', (e) => {
      const btn = e.target.closest('.preview-tab-btn');
      if (btn && btn.dataset.screen) selectScreen(btn.dataset.screen);
    });

    miniGrid.addEventListener('click', (e) => {
      const card = e.target.closest('.screen-mini-card');
      if (card && card.dataset.screen) selectScreen(card.dataset.screen);
    });

    selectScreen(APP_SCREENS[0].id);
  }

  function setupPricingConfigurator() {
    const scaleSelect = document.getElementById('studio-scale-select');
    const soloCapacity = document.getElementById('plan-solo-capacity');
    const proCapacity = document.getElementById('plan-pro-capacity');
    const agencyCapacity = document.getElementById('plan-agency-capacity');

    if (!scaleSelect) return;

    const presets = {
      standard: {
        solo: 'Up to 15 Active Events • Single Photographer',
        pro: 'Unlimited Active Events • Full QR & Guest Portal Suite',
        agency: 'Multi-Photographer Roles (Admin, Event, Support, Storage)'
      },
      high_volume: {
        solo: 'Up to 50 Active Events • Single Photographer',
        pro: 'Unlimited Events • High-Volume DSLR RAW + JPG Queue',
        agency: 'Multi-Studio Command Center + Custom Drive Folders'
      },
      enterprise: {
        solo: 'Self-Hosted Firebase + Personal Google Drive',
        pro: 'Dedicated Firestore & Custom OAuth Drive Bridge',
        agency: 'Full 14-Module Admin Governance & Audit Telemetry'
      }
    };

    scaleSelect.addEventListener('change', () => {
      const mode = presets[scaleSelect.value] || presets.standard;
      if (soloCapacity) soloCapacity.textContent = mode.solo;
      if (proCapacity) proCapacity.textContent = mode.pro;
      if (agencyCapacity) agencyCapacity.textContent = mode.agency;
    });
  }

  function setupFaqAccordion() {
    document.querySelectorAll('.faq-question').forEach((btn) => {
      btn.addEventListener('click', () => {
        const item = btn.closest('.faq-item');
        if (item) item.classList.toggle('open');
      });
    });
  }

  function setupPolicyModalsAndMenu() {
    const modal = document.getElementById('policy-modal');
    const modalTitle = document.getElementById('policy-modal-title');
    const modalBody = document.getElementById('policy-modal-body');
    const closeBtn = document.getElementById('policy-modal-close');

    const policies = {
      privacy: {
        title: 'Privacy Policy — Jummo Photos AI',
        html: `
          <p><strong>1. Explicit Guest Consent for AI Face Search:</strong> Guests scanning an event QR code must explicitly check and submit the consent agreement before any selfie face-search session is created.</p>
          <p><strong>2. Strict Per-Event Data Isolation:</strong> Every photo, QR code, and guest consent session is strictly scoped by <code>eventId</code> and <code>userId</code> in Cloud Firestore.</p>
          <p><strong>3. Instant Face-Data Purge Controls:</strong> Both guests and photographers can permanently purge guest face-search sessions at any time from the Guest Portal or Profile &rarr; Privacy settings.</p>
          <p><strong>4. Local Original Photo Preservation:</strong> Original captured and imported photos are stored in app-private storage (<code>filesDir/photos</code>) and are never automatically deleted after upload.</p>
        `
      },
      terms: {
        title: 'Terms of Service — Jummo Photos AI',
        html: `
          <p><strong>1. Studio Ownership:</strong> Photographers retain 100% ownership and copyright over all event photographs captured or imported into Jummo Photos AI.</p>
          <p><strong>2. Authorized Guest Distribution:</strong> Event QR codes can be configured for Full Gallery Access, Face Search Only, or Photographer Approval, and can be paused or expired at any time.</p>
          <p><strong>3. Cloud &amp; Drive Connectivity:</strong> Google Authentication is powered by Firebase Authentication, and Google Drive folder sync uses a clean architecture so client secrets are never embedded in the APK.</p>
        `
      }
    };

    document.querySelectorAll('[data-policy-modal]').forEach((trigger) => {
      trigger.addEventListener('click', (e) => {
        e.preventDefault();
        const key = trigger.getAttribute('data-policy-modal');
        const data = policies[key];
        if (data && modal && modalTitle && modalBody) {
          modalTitle.textContent = data.title;
          modalBody.innerHTML = data.html;
          modal.classList.add('open');
        }
      });
    });

    if (closeBtn && modal) {
      closeBtn.addEventListener('click', () => modal.classList.remove('open'));
      modal.addEventListener('click', (e) => {
        if (e.target === modal) modal.classList.remove('open');
      });
    }

    const menuBtn = document.getElementById('mobile-menu-toggle');
    const navLinks = document.getElementById('main-nav-links');
    if (menuBtn && navLinks) {
      menuBtn.addEventListener('click', () => {
        navLinks.classList.toggle('mobile-open');
      });
    }
  }

  document.addEventListener('DOMContentLoaded', () => {
    setupApkDownloadHandlers();
    setupScreenPreviewGallery();
    setupPricingConfigurator();
    setupFaqAccordion();
    setupPolicyModalsAndMenu();
    verifyGitHubReleaseApk();
  });
})();
