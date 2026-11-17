"""Security regression coverage for the shared generated-source safety policy."""
from __future__ import annotations
import importlib.util
from pathlib import Path
import sys
import pytest

SCRIPT = Path(__file__).resolve().parents[1] / 'bootstrap/clean_generated.py'
SPEC = importlib.util.spec_from_file_location('security_generated_cleanup', SCRIPT)
cleanup = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = cleanup
SPEC.loader.exec_module(cleanup)


def test_only_native_generated_directories_are_cleaned(tmp_path):
    product = tmp_path / 'artifact/src/product'
    for directory in ('java.gen', 'java', 'config.gen'):
        (product / directory).mkdir(parents=True)
        (product / directory / 'keep.txt').write_text('original')
    roots = cleanup.generated_directories(tmp_path, tmp_path)
    assert roots == [product / 'java.gen']
    cleanup.clean(tmp_path, tmp_path)
    assert not (product / 'java.gen/keep.txt').exists()
    assert (product / 'java/keep.txt').read_text() == 'original'
    assert (product / 'config.gen/keep.txt').read_text() == 'original'


def test_detects_only_declared_native_generated_roots(tmp_path):
    assert cleanup.generated_directories(tmp_path, tmp_path) == []
    (tmp_path / 'artifact/src/product/python.gen').mkdir(parents=True)
    assert cleanup.generated_directories(tmp_path, tmp_path) == [
        tmp_path / 'artifact/src/product/python.gen'
    ]


def test_symlinked_generated_directory_is_forbidden(tmp_path):
    product = tmp_path / 'artifact/src/product'
    product.mkdir(parents=True)
    (product / 'java.gen').symlink_to(tmp_path, target_is_directory=True)
    with pytest.raises(ValueError, match='symlinked generated directory'):
        cleanup.generated_directories(tmp_path, tmp_path)
