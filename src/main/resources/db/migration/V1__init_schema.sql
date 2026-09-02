CREATE TABLE bill_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE participants (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    group_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_participant_group FOREIGN KEY (group_id) REFERENCES bill_groups(id) ON DELETE CASCADE
);

CREATE INDEX idx_participant_group ON participants(group_id);

CREATE TABLE expenses (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    paid_by_participant_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_expense_group FOREIGN KEY (group_id) REFERENCES bill_groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_expense_paid_by FOREIGN KEY (paid_by_participant_id) REFERENCES participants(id)
);

CREATE INDEX idx_expense_group ON expenses(group_id);
CREATE INDEX idx_expense_paid_by ON expenses(paid_by_participant_id);

CREATE TABLE expense_participants (
    expense_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    PRIMARY KEY (expense_id, participant_id),
    CONSTRAINT fk_ep_expense FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE,
    CONSTRAINT fk_ep_participant FOREIGN KEY (participant_id) REFERENCES participants(id) ON DELETE CASCADE
);

CREATE INDEX idx_ep_expense ON expense_participants(expense_id);
CREATE INDEX idx_ep_participant ON expense_participants(participant_id);
