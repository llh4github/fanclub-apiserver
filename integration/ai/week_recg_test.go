package ai

import (
	"context"
	"os"
	"testing"
	"time"

	"fanclub-apiserver/ai"
	"fanclub-apiserver/g"
	"fanclub-apiserver/integration/testenv"
)

func init() {
	g.Cfg = &g.Config{
		ARK: g.ARKConfig{
			APIKey:    "",
			BaseURL:   "https://ark.cn-beijing.volces.com/api/v3",
			ModelName: "doubao-seed-2-0-lite-260215",
		},
	}
}

func TestInitArkClient(t *testing.T) {
	tests := []struct {
		name    string
		apiKey  string
		wantErr bool
	}{
		{
			name:    "empty api key should not init",
			apiKey:  "",
			wantErr: false,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			g.Cfg.ARK.APIKey = tt.apiKey

			err := ai.InitArkClient()
			if (err != nil) != tt.wantErr {
				t.Errorf("InitArkClient() error = %v, wantErr %v", err, tt.wantErr)
			}
		})
	}
}

func TestRecognizeScheduleFromURL_NilClient(t *testing.T) {
	err := ai.RecognizeScheduleFromURL(context.Background(), "https://example.com", 0)
	if err == nil {
		t.Error("Expected error when chatModel is nil, got nil")
	}

	expectedMsg := "ARK client not initialized"
	if err.Error() != expectedMsg {
		t.Errorf("Expected error message %q, got %q", expectedMsg, err.Error())
	}
}

func TestExtractImageURLs(t *testing.T) {
	tests := []struct {
		name      string
		url       string
		expectURL bool
	}{
		{
			name:      "bilibili opus page",
			url:       "https://www.bilibili.com/opus/1159880489834643463",
			expectURL: true,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			urls, err := ai.ExtractImageURLs(tt.url)
			if err != nil {
				t.Logf("Warning: extractImageURLs failed: %v", err)
				t.Skip("Skipping due to network error")
			}

			t.Logf("Found %d images", len(urls))
			for i, u := range urls {
				t.Logf("Image %d: %s", i+1, u)
			}

			if tt.expectURL && len(urls) == 0 {
				t.Error("Expected to find images but got none")
			}
		})
	}
}

func TestRecognizeScheduleFromURL_Integration(t *testing.T) {
	t.Parallel()

	apiKey := os.Getenv("ARK_API_KEY")
	if apiKey == "" {
		t.Skip("ARK_API_KEY not set, skipping integration test")
	}

	env, err := testenv.GetTestEnvironment()
	if err != nil {
		t.Fatalf("Failed to create test environment: %v", err)
	}
	defer env.Cleanup()

	g.Cfg.ARK.APIKey = apiKey
	g.Cfg.ARK.Timeout = new(120 * time.Second)
	err = ai.InitArkClient()
	if err != nil {
		t.Fatalf("Failed to init ARK client: %v", err)
	}

	tests := []struct {
		name string
		url  string
	}{
		{
			name: "bilibili opus page",
			url:  "https://www.bilibili.com/opus/1159880489834643463",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			err := ai.RecognizeScheduleFromURL(context.Background(), tt.url, 0)
			if err != nil {
				t.Fatalf("RecognizeScheduleFromURL() error = %v", err)
			}

			t.Logf("Schedule recognition completed successfully")
		})
	}
}
