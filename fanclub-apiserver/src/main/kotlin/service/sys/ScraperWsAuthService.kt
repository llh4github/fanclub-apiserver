/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.sys

import llh.fanclubvup.apiserver.entity.sys.ScraperWsAuth
import llh.fanclubvup.apiserver.service.BaseDatabaseService
import llh.fanclubvup.bilisdk.scraper.BiliWsAuthFetcher

interface ScraperWsAuthService : BaseDatabaseService<ScraperWsAuth>, BiliWsAuthFetcher
