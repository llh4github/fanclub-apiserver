-- AI 总结表
CREATE TABLE IF NOT EXISTS treehole_submission_summaries (
    id BIGINT PRIMARY KEY,
    created_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    submission_id BIGINT NOT NULL UNIQUE,
    content TEXT NOT NULL,
    CONSTRAINT fk_submission_summary_submission FOREIGN KEY (submission_id) 
        REFERENCES treehole_submissions(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_submission_summary_submission_id 
    ON treehole_submission_summaries(submission_id);

COMMENT ON TABLE treehole_submission_summaries IS 'AI对投稿内容的总结记录';
COMMENT ON COLUMN treehole_submission_summaries.submission_id IS '关联的投稿数据ID，外键引用treehole_submissions.id';
COMMENT ON COLUMN treehole_submission_summaries.content IS 'AI总结内容，Markdown格式';