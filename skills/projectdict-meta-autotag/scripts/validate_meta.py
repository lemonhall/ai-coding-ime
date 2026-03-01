#!/usr/bin/env python3
"""
Validate .ime/meta.json for ProjectDict D01-D74 constraints.

This script intentionally does NOT scan projects or recommend profiles.
It only validates structure and allowed profile scope.
"""

from __future__ import annotations

import argparse
import json
from datetime import datetime
from pathlib import Path
from typing import Any, List

ALLOWED_PROFILES = {
    "hardware.cpu-architecture",
    "hardware.embedded-iot",
    "hardware.os-kernel",
    "hardware.driver-hal",
    "hardware.fpga-hdl",
    "network.protocols",
    "network.web-backend-api",
    "frontend",
    "network.security-crypto",
    "network.distributed-systems",
    "data.relational-db",
    "data.nosql-newsql",
    "data.engineering-etl",
    "data.search-ir",
    "engineering.compiler-interpreter",
    "engineering.plt",
    "engineering.software-architecture",
    "engineering.devops-sre",
    "engineering.vcs-collaboration",
    "engineering.testing",
    "engineering.package-build",
    "app.android",
    "client.desktop-cross-platform",
    "client.game-dev",
    "client.graphics-rendering",
    "ai.classic-ml",
    "ai.deep-learning",
    "ai.nlp-llm",
    "ai.computer-vision",
    "ai.recommender-systems",
    "ai.mlops",
    "ai.reinforcement-learning",
    "science.scientific-computing",
    "science.physics-simulation-fem",
    "science.signal-processing-dsp",
    "science.computational-geometry-cad",
    "science.bioinformatics",
    "science.quantum-computing",
    "infra.cloud-iaas",
    "infra.containers-orchestration",
    "infra.mq-event-streaming",
    "infra.observability-monitoring",
    "infra.iac",
    "domain.blockchain-web3",
    "domain.audio-video-streaming",
    "domain.gis",
    "domain.robotics-ros",
    "domain.editor-ide-tooling",
    "domain.data-visualization",
    "domain.lowcode-dsl-config",
    "business.erp",
    "business.crm",
    "business.hrm",
    "business.finance-payments",
    "business.supply-chain-logistics-wms",
    "business.ecommerce-platform",
    "business.oa-workflow",
    "product.management",
    "product.growth-operations",
    "product.adtech-martech",
    "product.content-community-ops",
    "product.seo-sem-aso",
    "product.analytics-bi",
    "product.ab-testing",
    "industry.fintech",
    "industry.insurtech",
    "industry.healthtech",
    "industry.edtech",
    "industry.proptech",
    "industry.mobility-lbs",
    "industry.saas-subscription",
    "compliance.data-privacy",
    "compliance.infosec",
    "compliance.cn-regulation",
}

MANDATORY_PROFILE = "engineering.testing"
REQUIRED_TOP_LEVEL_KEYS = ("version", "project", "lang", "generated_at", "dict_profiles")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Validate .ime/meta.json against D01-D74 rules")
    parser.add_argument("--meta", default=".ime/meta.json", help="Path to meta.json")
    parser.add_argument(
        "--print-allowed",
        action="store_true",
        help="Print allowed D01-D74 profile list and exit",
    )
    return parser.parse_args()


def load_json(path: Path) -> Any:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except FileNotFoundError:
        raise SystemExit(f"ERROR: file not found: {path}")
    except json.JSONDecodeError as exc:
        raise SystemExit(f"ERROR: invalid JSON: {exc}")


def validate_iso8601(value: str) -> bool:
    if not isinstance(value, str) or not value:
        return False
    normalized = value.replace("Z", "+00:00")
    try:
        datetime.fromisoformat(normalized)
        return True
    except ValueError:
        return False


def validate_profiles(profiles: Any, errors: List[str]) -> List[str]:
    if not isinstance(profiles, list):
        errors.append("dict_profiles must be a list")
        return []

    if len(profiles) != 5:
        errors.append(f"dict_profiles must contain exactly 5 items, got {len(profiles)}")

    if not all(isinstance(x, str) and x.strip() for x in profiles):
        errors.append("dict_profiles must contain non-empty strings only")
        return []

    clean = [x.strip() for x in profiles]
    if len(set(clean)) != len(clean):
        errors.append("dict_profiles must not contain duplicates")

    missing = [x for x in clean if x not in ALLOWED_PROFILES]
    if missing:
        errors.append(f"dict_profiles contains out-of-range profiles: {', '.join(sorted(set(missing)))}")

    if MANDATORY_PROFILE not in clean:
        errors.append(f"dict_profiles must include mandatory profile: {MANDATORY_PROFILE}")

    return clean


def validate_tags(tags: Any, dict_profiles: List[str], errors: List[str]) -> None:
    if tags is None:
        return
    if not isinstance(tags, list):
        errors.append("tags must be a list when present")
        return
    if not all(isinstance(x, str) and x.strip() for x in tags):
        errors.append("tags must contain non-empty strings only")
        return

    clean = [x.strip() for x in tags]
    extra = [x for x in clean if x not in ALLOWED_PROFILES]
    if extra:
        errors.append(f"tags contains out-of-range profiles: {', '.join(sorted(set(extra)))}")

    missing_profiles = [x for x in dict_profiles if x not in clean]
    if missing_profiles:
        errors.append(
            "tags must include all dict_profiles, missing: "
            + ", ".join(sorted(set(missing_profiles)))
        )


def validate_meta(data: Any) -> List[str]:
    errors: List[str] = []
    if not isinstance(data, dict):
        return ["top-level JSON value must be an object"]

    for key in REQUIRED_TOP_LEVEL_KEYS:
        if key not in data:
            errors.append(f"missing required key: {key}")

    version = data.get("version")
    if not isinstance(version, int):
        errors.append("version must be an integer")
    elif version != 1:
        errors.append(f"version must be 1, got {version}")

    project = data.get("project")
    if not isinstance(project, str) or not project.strip():
        errors.append("project must be a non-empty string")

    lang = data.get("lang")
    if not isinstance(lang, list) or not lang or not all(isinstance(x, str) and x.strip() for x in lang):
        errors.append("lang must be a non-empty string list")

    generated_at = data.get("generated_at")
    if not validate_iso8601(generated_at):
        errors.append("generated_at must be valid ISO-8601 datetime string")

    dict_profiles = validate_profiles(data.get("dict_profiles"), errors)
    validate_tags(data.get("tags"), dict_profiles, errors)

    return errors


def main() -> int:
    args = parse_args()
    if args.print_allowed:
        for slug in sorted(ALLOWED_PROFILES):
            print(slug)
        return 0

    meta_path = Path(args.meta).resolve()
    payload = load_json(meta_path)
    errors = validate_meta(payload)
    if errors:
        for err in errors:
            print(f"ERROR: {err}")
        return 1

    dict_profiles = payload["dict_profiles"]
    print(f"VALID: {meta_path}")
    print(f"dict_profiles(5): {', '.join(dict_profiles)}")
    print(f"mandatory profile present: {MANDATORY_PROFILE}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
